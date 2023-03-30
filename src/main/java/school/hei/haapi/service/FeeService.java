package school.hei.haapi.service;

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
import school.hei.haapi.model.InterestHistory;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.repository.FeeRepository;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;

@Service
@AllArgsConstructor
@Slf4j
public class FeeService {

  private static final school.hei.haapi.endpoint.rest.model.Fee.StatusEnum DEFAULT_STATUS = LATE;
  private final FeeRepository feeRepository;
  private final FeeValidator feeValidator;

  private final EventProducer eventProducer;

  private final DelayPenaltyService delayPenaltyService;
  private final InterestHistoryService interestHistoryService;
  private final PaymentService paymentService;

  public Fee getById(String id) {
    return updateFeeStatus(feeRepository.getById(id));
  }

  public Fee getByStudentIdAndFeeId(String studentId, String feeId) {
    return updateFeeStatus(feeRepository.getByStudentIdAndId(studentId, feeId));
  }

  @Transactional
  public List<Fee> saveAll(List<Fee> fees) {
    feeValidator.accept(fees);
    return feeRepository.saveAll(fees);
  }

  public List<Fee> getFees(
      PageFromOne page, BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "dueDatetime"));
    if (status != null) {
      return feeRepository.getFeesByStatus(status, pageable);
    }
    return feeRepository.getFeesByStatus(DEFAULT_STATUS, pageable);
  }

  public List<Fee> getFeesByStudentId(
      String studentId, PageFromOne page, BoundedPageSize pageSize,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(DESC, "dueDatetime"));

    List<Fee> feeList;

    feeList = (status != null)?
            feeRepository.getFeesByStudentIdAndStatus(studentId, status, pageable) :
            feeRepository.getByStudentId(studentId, pageable);
    for (Fee fee:feeList ) {
      updateLateFee(fee);
    }

    return feeList;
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



  private int computeTotalAmount(school.hei.haapi.model.Fee currentFee){
    return currentFee.getTotalAmount()+interestHistoryService.getInterestAmount(currentFee.getId());
  }
  private int computeRemainingAmountAmount(school.hei.haapi.model.Fee currentFee){
    int sumPayment = 0;
    List<Payment> payments = paymentService.getAllPaymentByStudentIdAndFeeId(currentFee.getStudent().getId(),currentFee.getId());
    for (Payment p:payments) {
      sumPayment = sumPayment + p.getAmount();
    }
    return computeTotalAmount(currentFee)-sumPayment;
  }

  private int convertInterestTimeRateToDayNumber( school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum interestTimeRate){
    switch (interestTimeRate) {
      case DAILY:
        return 1;
      default:
        throw new BadRequestException(
                "Unexpected delay Penalty Interest Time rate: " + interestTimeRate.getValue());
    }
  }

  private Fee updateLateFee(Fee fee){
    if (!fee.getStatus().equals(school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID)){
      ZoneId zone = ZoneId.of("UTC");
      LocalDate today = LocalDate.ofInstant(Instant.now(), zone);
      LocalDate dueTime = LocalDate.ofInstant(fee.getDueDatetime(), zone);
      DelayPenalty generalRule = delayPenaltyService.getAll().get(0);
      if ( Period.between(today, dueTime.plusDays(generalRule.getGraceDelay())).getDays()>=0){
        List<InterestHistory> interestHistories = interestHistoryService.getAllByFeeId(fee.getId());
        if (interestHistories.size()==0){
          //toDO create InterestHistory
          java.util.Date InterestStart = java.util.Date.from(dueTime.plusDays(generalRule.getGraceDelay()+1).atTime(12,0).toInstant(ZoneOffset.UTC));
          java.util.Date InterestEnd = java.util.Date.from(dueTime.plusDays(generalRule.getGraceDelay()+1+ generalRule.getApplicabilityDelayAfterGrace()).atTime(12,0).toInstant(ZoneOffset.UTC));
          InterestHistory toCreate = InterestHistory.builder()
                  .fee(fee)
                  .interestRate(generalRule.getInterestPercent())
                  .interestTimeRate(convertInterestTimeRateToDayNumber(generalRule.getInterestTimeRate()))
                  .interestStart(InterestStart)
                  .interestEnd(InterestEnd)
                  .build();
          interestHistoryService.saveAll(List.of(toCreate));
          interestHistories = interestHistoryService.getAllByFeeId(fee.getId());
        }
        fee.setTotalAmount(computeTotalAmount(fee));
        fee.setRemainingAmount(computeRemainingAmountAmount(fee));
      }
    }
    return fee;
  }
}
