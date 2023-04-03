package school.hei.haapi.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
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
import school.hei.haapi.model.DelayPenalty;
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
    //Only for test
    return applyGraceDelay(List.of(updateFeeStatus(feeRepository.getById(id)))).get(0);
    //return updateFeeStatus(feeRepository.getById(id))
  }

  public Fee getByStudentIdAndFeeId(String studentId, String feeId) {
    //Only for test
    return applyGraceDelay(List.of(
        updateFeeStatus(feeRepository.getByStudentIdAndId(studentId, feeId)))).get(0);
    //return updateFeeStatus(feeRepository.getByStudentIdAndId(studentId, feeId));
  }

  @Transactional
  public List<Fee> saveAll(List<Fee> fees) {
    List<Fee> toSave = applyGraceDelay(fees);
    return feeRepository.saveAll(toSave);
  }

  public List<Fee> getFees(
      PageFromOne page, BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "dueDatetime"));
    if (status != null) {
      //We use this method here just for testing, but it should be inside a scheduller
      return applyGraceDelay(feeRepository.getFeesByStatus(status, pageable));
      //return feeRepository.getFeesByStatus(status, pageable);
    }
    return applyGraceDelay(feeRepository.getFeesByStatus(DEFAULT_STATUS, pageable));
    //return feeRepository.getFeesByStatus(DEFAULT_STATUS, pageable);
  }

  public List<Fee> getFeesByStudentId(
      String studentId, PageFromOne page, BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(DESC, "dueDatetime"));
    if (status != null) {
      //Only for testing, remove after
      return applyGraceDelay(feeRepository.getFeesByStudentIdAndStatus(studentId, status,
          pageable));
      //return feeRepository.getFeesByStudentIdAndStatus(studentId, status, pageable);
    }
    //Only for testing, remove after
    return applyGraceDelay(feeRepository.getByStudentId(studentId, pageable));
    //return feeRepository.getByStudentId(studentId, pageable);
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
    feeRepository.saveAll(unpaidFees);
  }

  @Scheduled(cron = "0 0 * * * *")
  public void updateFeesDueDatetime() {
    List<Fee> unpaidFees = feeRepository.findAll();
    //Here is the right place of this method
    feeRepository.saveAll(applyGraceDelay(unpaidFees));
  }

  @Scheduled(cron = "0 0 0 * * *")
  public void updateFeesInterestPercent() {
    List<Fee> lateFees = feeRepository.getFeesByStatus(LATE);
    feeRepository.saveAll(applyInterestPercent(lateFees));
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

  private List<Fee> applyGraceDelay(List<Fee> fees) {
    for (Fee fee : fees) {
      if (!fee.isUpToDate()) {
        //Apply grace delay to the default due datetime
        DelayPenalty globalConf = delayPenaltyService.getDelayPenaltyGlobalConf();
        Optional<DelayPenalty> individualConf =
            delayPenaltyService.getIndividualDelayPenalty(fee.getStudent().getId());
        if (individualConf.isPresent()) {
          fee.setDueDatetime(fee.getDueDatetime().plus(
              individualConf.get().getGraceDelay(),
              ChronoUnit.DAYS));
          fee.setUpToDate(true);
        } else {
          fee.setDueDatetime(fee.getDueDatetime().plus(
              globalConf.getGraceDelay(),
              ChronoUnit.DAYS));
          fee.setUpToDate(true);
        }
      }
    }
    return fees;
  }

  private List<Fee> applyInterestPercent(List<Fee> fees) {
    for (Fee fee : fees) {
      Instant applicabilityDatetime = fee.getDueDatetime().plus(
          delayPenaltyService.getDelayPenaltyGlobalConf().getApplicabilityDelayAfterGrace(),
          ChronoUnit.DAYS);
      int remainingAmount = fee.getTotalAmount() - paymentsTotalAmount(fee);
      int interestValue = remainingAmount * delayPenaltyService
          .getDelayPenaltyGlobalConf().getInterestPercent() / 100;
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
