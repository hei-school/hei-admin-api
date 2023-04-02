package school.hei.haapi.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.TypedLateFeeVerified;
import school.hei.haapi.endpoint.event.model.gen.LateFeeVerified;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Payment;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.PaymentRepository;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;

@Service
@AllArgsConstructor
@Slf4j
public class FeeService {

  private static final school.hei.haapi.endpoint.rest.model.Fee.StatusEnum DEFAULT_STATUS = LATE;
  private final FeeRepository feeRepository;
  private final DelayPenaltyService delayPenaltyService;
  private final EventProducer eventProducer;
  private final PaymentRepository paymentRepository;

  public Fee getById(String id) {
    return updateFeeStatus(feeRepository.getById(id));
  }

  public Fee getByStudentIdAndFeeId(String studentId, String feeId) {
    return updateFeeStatus(feeRepository.getByStudentIdAndId(studentId, feeId));
  }

  @Transactional
  public List<Fee> saveAll(List<Fee> fees) {
    return feeRepository.saveAll(fees);
  }

  public List<Fee> getFees(
      PageFromOne page, BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "dueDatetime"));
    if (status != null) {
      //We use this method here just for testing, but it should be inside a scheduller
      return applyDelayPenaltyConf(feeRepository.getFeesByStatus(status, pageable));
    }
    return applyDelayPenaltyConf(feeRepository.getFeesByStatus(DEFAULT_STATUS, pageable));
  }

  public List<Fee> getFeesByStudentId(
      String studentId, PageFromOne page, BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(DESC, "dueDatetime"));
    if (status != null) {
      return applyDelayPenaltyConf(
          feeRepository.getFeesByStudentIdAndStatus(studentId, status, pageable));
    }
    return applyDelayPenaltyConf(feeRepository.getByStudentId(studentId, pageable));
  }

  private Fee updateFeeStatus(Fee initialFee) {
    if (initialFee.getRemainingAmount() == 0) {
      initialFee.setStatus(PAID);
    } else if (Instant.now().isAfter(initialFee.getDueDatetime())) {
      initialFee.setStatus(LATE);
    }
    return initialFee;
  }

  @Scheduled(cron = "0 0 * * * *")
  public void updateFeesStatusToLate() {
    List<Fee> unpaidFees = feeRepository.getUnpaidFees();
    unpaidFees.forEach(fee -> {
      updateFeeStatus(fee);
      log.info("Fee with id." + fee.getId() + " is going to be updated from UNPAID to LATE");
    });
    //Here is the right place of this method
    feeRepository.saveAll(applyDelayPenaltyConf(unpaidFees));
  }

  private TypedLateFeeVerified toTypedEvent(Fee fee) {
    return new TypedLateFeeVerified(
        LateFeeVerified.builder()
            .type(fee.getType())
            .student(fee.getStudent())
            .comment(fee.getComment())
            .remainingAmount(fee.getRemainingAmount())
            .dueDatetime(fee.getDueDatetime())
            .build()
    );
  }

  /*
   * An email will be sent to user with late fees
   * every morning at 8am (UTC+3)
   * */
  @Scheduled(cron = "0 0 8 * * *")
  public void sendLateFeesEmail() {
    List<Fee> lateFees = feeRepository.getFeesByStatus(LATE);
    lateFees.forEach(
        fee -> {
          eventProducer.accept(List.of(toTypedEvent(fee)));
          log.info("Late Fee with id." + fee.getId() + " is sent to Queue");
        }
    );
  }

  private List<Fee> applyDelayPenaltyConf(List<Fee> fees) {
    for (Fee fee : fees) {
      if (!fee.isUpToDate()) {
        //Apply grace delay to the default due datetime
        fee.setDueDatetime(fee.getDueDatetime().plus(
            delayPenaltyService.getDelayPenaltyConf().getGraceDelay(),
            ChronoUnit.DAYS));
        fee.setUpToDate(true);
      }
      Instant applicabilityDatetime = fee.getDueDatetime().plus(
          delayPenaltyService.getDelayPenaltyConf().getApplicabilityDelayAfterGrace(),
          ChronoUnit.DAYS);
      int remainingAmount = fee.getTotalAmount() - paymentsTotalAmount(fee);
      int interestValue = remainingAmount * delayPenaltyService
          .getDelayPenaltyConf().getInterestPercent() / 100;
      if (fee.getStatus().equals(LATE) && Instant.now().isBefore(applicabilityDatetime)) {
        fee.setRemainingAmount(remainingAmount + interestValue);
      }
    }
    return fees;
  }

  private int paymentsTotalAmount(Fee fee) {
    List<Integer> payments = paymentRepository.getPaymentsByFeeId(fee.getId())
        .stream().map(Payment::getAmount).collect(Collectors.toUnmodifiableList());
    return payments.stream().reduce(0, Integer::sum);
  }
}
