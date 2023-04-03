package school.hei.haapi.service;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.repository.FeeRepository;

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
    List<Fee> lateFees = feeRepository.getFeesByStatusAndStudentId(LATE, studentId);
    applyLatePenalties(lateFees);
    if (status != null) {
      return feeRepository.getFeesByStudentIdAndStatus(studentId, status, pageable);
    }
    return feeRepository.getByStudentId(studentId, pageable);
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

  public List<Fee> getFeesByStatus(school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    return feeRepository.getFeesByStatus(status);
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

public void applyLatePenalties(List<Fee> lateFees) {
  DelayPenalty penalty = delayPenaltyService.getDelayPenalty();
  int interestPercent = penalty.getInterestPercent();
  int graceDelay = penalty.getGraceDelay();
  int applicabilityAfterGrace = penalty.getApplicabilityDelayAfterGrace();

  lateFees.forEach(
          fee -> {
            Instant due = fee.getDueDatetime();

            Instant dueGrace = due.plus(graceDelay, ChronoUnit.DAYS);
            Instant afterGrace = due.plus(applicabilityAfterGrace, ChronoUnit.DAYS);

            Instant nextApplyInterest = fee.getLastInterestPenality() != null ? fee.getLastInterestPenality().plus(24, ChronoUnit.HOURS) : null;
            Instant tentativeApplyInterest = Instant.now();


            if (dueGrace.isAfter(tentativeApplyInterest) &&
                    afterGrace.isBefore(tentativeApplyInterest) &&
                    (nextApplyInterest == null || nextApplyInterest.isAfter(tentativeApplyInterest))
            ) {
              int timeDelta = fee.getLastInterestPenality() != null ? fee.getLastInterestPenality().get(ChronoField.DAY_OF_MONTH) - tentativeApplyInterest.get(ChronoField.DAY_OF_MONTH) : 1;
              int newAmount = fee.getRemainingAmount() + ((fee.getRemainingAmount() * interestPercent / 100) * timeDelta);
              fee.setRemainingAmount(newAmount);
              fee.setLastInterestPenality(tentativeApplyInterest);
            }
            if(afterGrace.isAfter(tentativeApplyInterest)) log.info("Ito fy ito zany efa ...");
          }
  );

  feeRepository.saveAll(lateFees);
}

}
