package school.hei.haapi.service;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;
import java.time.Instant;
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
import school.hei.haapi.repository.DelayPenaltyRepository;
import school.hei.haapi.repository.FeeRepository;

@Service
@AllArgsConstructor
@Slf4j
public class FeeService {

  private static final school.hei.haapi.endpoint.rest.model.Fee.StatusEnum DEFAULT_STATUS = LATE;
  private final FeeRepository feeRepository;
  private final FeeValidator feeValidator;

  private final EventProducer eventProducer;
  private final DelayPenaltyRepository delayPenaltyRepository;

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
    List<Fee> specifiedLateFees = feeRepository.getFeesByStatusAndStudentId(LATE, studentId);
    DelayPenalty specificCondition = delayPenaltyRepository.getByStudentIdAndStatus(studentId, DelayPenalty.StatusEnum.SPECIFIC);
    DelayPenalty globalCondition = delayPenaltyRepository.getByStatus(DelayPenalty.StatusEnum.GLOBAL);
    applyInterestPercent(specifiedLateFees, Instant.now(), (specificCondition != null ? specificCondition : globalCondition));
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(DESC, "dueDatetime"));
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

  @Scheduled(cron = "0 0 * * * *")
  public void automateApplyInterest() {
    List<Fee> lateFees = feeRepository.getFeesByStatus(LATE);
    DelayPenalty condition = delayPenaltyRepository.findAll().get(0);
    applyInterestPercent(lateFees, Instant.now(), condition);
  }

  public void applyInterestPercent(List<Fee> lateFees, Instant attemptApplyInterest, DelayPenalty condition) {
    int interestPercent = condition.getInterestPercent();
    int graceDelay = condition.getGraceDelay();
    int applicabilityAfterGrace = condition.getApplicabilityDelayAfterGrace();

    lateFees.forEach(
        fee -> {
          Instant dueGrace = fee.getDueDatetime().plus(graceDelay, DAYS);
          Instant dateAfterGrace = dueGrace.plus(applicabilityAfterGrace, DAYS);

          int timeDelta = numberOfDayForInterest(dateAfterGrace, fee.getLastApplyInterest(),
              dueGrace, attemptApplyInterest, fee.getId());
          if (timeDelta > 0) {
            int newAmount = fee.getRemainingAmount();
            for (int time = 1; time <= timeDelta; time++) {
              newAmount += (newAmount * interestPercent / 100);
            }
            fee.setRemainingAmount(newAmount);
            fee.setLastApplyInterest(attemptApplyInterest);
            feeRepository.save(fee);
          }
        }
    );
  }

  public int numberOfDayForInterest(Instant lastLuckyDay,
                                    Instant lastApplyInterest,
                                    Instant dueGrace,
                                    Instant attemptDate,
                                    String feeId) {

    if (lastLuckyDay.isAfter(attemptDate)) {
      log.info("Efa tapitra ny fotoana tokony handoavana ny saram-pianarana laharana: " + feeId);
    }
    if (lastApplyInterest != null) {
      return (int) DAYS.between(lastApplyInterest,
          (lastLuckyDay.isBefore(attemptDate) ? lastLuckyDay : attemptDate));
    } else {
      return (int) DAYS.between(dueGrace,
          (lastLuckyDay.isBefore(attemptDate) ? lastLuckyDay : attemptDate));
    }
  }
}
