package school.hei.haapi.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.Fee.StatusEnum;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Fee;
import school.hei.haapi.repository.DelayPenaltyRepository;
import school.hei.haapi.repository.FeeRepository;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
  private final DelayPenaltyRepository repository;
  private final FeeRepository feeRepository;

  public DelayPenalty getDelayPenalty() {
    return repository.findAll(Sort.by(Sort.Direction.DESC, "creationDatetime")).get(0);
  }

  public DelayPenalty crupdateDelayPenalty(DelayPenalty delayPenalty) {
    DelayPenalty saved = repository.save(delayPenalty);
    new Thread(this::applyDelayPenalty).start();
    return saved;
  }

  @Scheduled(cron = "0 0 0 * * *")
  public void applyDelayPenalty() {
    List<Fee> lateFees = feeRepository.getFeesByStatus(StatusEnum.LATE);
    DelayPenalty currentDelayPenalty = this.getDelayPenalty();
    int applicabilityDelayAfterGrace = currentDelayPenalty.getApplicabilityDelayAfterGrace();
    List<Fee> updatedLateFees = new ArrayList<>();
    lateFees.forEach(lateFee -> {
      Instant dueDateTime = lateFee.getDueDatetime();
      int graceDelay = lateFee.getStudent().getStudentGraceDelay() == null ?
          currentDelayPenalty.getGraceDelay() : lateFee.getStudent().getStudentGraceDelay();
      if (dueDateTime.plus(graceDelay, ChronoUnit.DAYS).isBefore(Instant.now())
          && dueDateTime
          .plus(graceDelay + applicabilityDelayAfterGrace, ChronoUnit.DAYS)
          .isAfter(Instant.now())) {
        long passedDays =
            dueDateTime.plus(graceDelay, ChronoUnit.DAYS).until(Instant.now(), ChronoUnit.DAYS) + 1;
        int actualRemainingAmount = lateFee.getRemainingAmount();
        int actualTotalAmount = lateFee.getTotalAmount();
        int updatedRemainingAmount = (int) (actualRemainingAmount *
            Math.pow(1 + ((double) currentDelayPenalty.getInterestPercent() / 100), passedDays));
        lateFee.setInterestAmount(updatedRemainingAmount - actualRemainingAmount);
        lateFee.setRemainingAmountWithInterest(updatedRemainingAmount);
        lateFee.setTotalAmountWithInterest(
            actualTotalAmount + updatedRemainingAmount - actualRemainingAmount);
        updatedLateFees.add(lateFee);
      } else if (dueDateTime.plus(graceDelay, ChronoUnit.DAYS).isBefore(Instant.now()) &&
          dueDateTime
              .plus(graceDelay + applicabilityDelayAfterGrace, ChronoUnit.DAYS)
              .isBefore(Instant.now())) {
        long passedDays = applicabilityDelayAfterGrace;
        int actualRemainingAmount = lateFee.getRemainingAmount();
        int actualTotalAmount = lateFee.getTotalAmount();
        int updatedRemainingAmount = (int) (actualRemainingAmount *
            Math.pow(1 + ((double) currentDelayPenalty.getInterestPercent() / 100), passedDays));
        lateFee.setInterestAmount(updatedRemainingAmount - actualRemainingAmount);
        lateFee.setRemainingAmountWithInterest(updatedRemainingAmount);
        lateFee.setTotalAmountWithInterest(
            actualTotalAmount + updatedRemainingAmount - actualRemainingAmount);
        updatedLateFees.add(lateFee);
      }
    });
    feeRepository.saveAll(updatedLateFees);
  }
}
