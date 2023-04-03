package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Fee;
import school.hei.haapi.repository.DelayPenaltyRepository;
import school.hei.haapi.repository.FeeRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    private final DelayPenaltyRepository penaltyRepository;
    private final FeeRepository feeRepository;

    public DelayPenalty getActualDelayPenalty() {
        List<DelayPenalty> penalties = penaltyRepository.findAll();
        if (penalties.isEmpty()) {
            throw new NoSuchElementException("No delay penalty found");
        }
        final int FIRST_PENALTY_INDEX = 0;
        return penalties.get(FIRST_PENALTY_INDEX);
    }

    public DelayPenalty modifyActualPenalty(DelayPenalty delayPenalty) {
        if (delayPenalty == null) {
            throw new IllegalArgumentException("Cannot modify null DelayPenalty object");
        }
        DelayPenalty newDelayPenalty = penaltyRepository.save(delayPenalty);
        Instant instantUpdateValue = Instant.parse("2023-03-16T08:30:24.00Z");
        List<Fee> fees = feeRepository.findAll();
        applyLateFees(fees, newDelayPenalty ,instantUpdateValue);
        return delayPenalty;
    }




    public void applyLateFees(List<Fee> fees, DelayPenalty delayPenalty, Instant instantUpdateValue) {
        int interestRate = delayPenalty.getInterestPercent();
        int graceDelayInDays = delayPenalty.getGraceDelay();
        int delayApplicabilityPeriodInDays = delayPenalty.getApplicabilityDelayAfterGrace();
        for (Fee fee : fees) {
            Instant dueDateTime = fee.getDueDatetime();
            Instant dayToApplyDelayPenalty = dueDateTime.plus(Duration.ofDays(graceDelayInDays));
            if (fee.getStatus() == LATE && instantUpdateValue.isAfter(dayToApplyDelayPenalty) && (fee.getUpdatedAt().isBefore(dayToApplyDelayPenalty) || fee.getUpdatedAt().isAfter(dayToApplyDelayPenalty.plus(Duration.ofDays(delayApplicabilityPeriodInDays))))) {
                long daysLate = ChronoUnit.DAYS.between(dueDateTime, instantUpdateValue);
                long numberOfDaysToApplyPenalty = Math.min(daysLate - graceDelayInDays, delayApplicabilityPeriodInDays);
                if (numberOfDaysToApplyPenalty > 0) {
                    double lateFeeAmount = FeeService.calculateCompoundInterest(fee.getRemainingAmount(), interestRate, numberOfDaysToApplyPenalty);
                    fee.setTotalAmount((int) lateFeeAmount);
                    fee.setRemainingAmount((int) lateFeeAmount);
                    fee.setUpdatedAt(instantUpdateValue);
                }
            } else if (fee.getStatus() == LATE && instantUpdateValue.isAfter(dayToApplyDelayPenalty) && fee.getUpdatedAt().isAfter(dayToApplyDelayPenalty) && fee.getUpdatedAt().isBefore(dayToApplyDelayPenalty.plus(Duration.ofDays(delayApplicabilityPeriodInDays)))) {
                long daysLate = ChronoUnit.DAYS.between(dueDateTime, instantUpdateValue);
                long daysBetweenUpdateAndDaytoApplyDelayPenalty = ChronoUnit.DAYS.between(dayToApplyDelayPenalty, fee.getUpdatedAt());
                long numberOfDaysToApplyPenalty = Math.min((daysLate - graceDelayInDays) - daysBetweenUpdateAndDaytoApplyDelayPenalty, delayApplicabilityPeriodInDays - daysBetweenUpdateAndDaytoApplyDelayPenalty);
                if (numberOfDaysToApplyPenalty > 0) {
                    double lateFeeAmount = FeeService.calculateCompoundInterest(fee.getRemainingAmount(), interestRate, numberOfDaysToApplyPenalty);
                    fee.setTotalAmount((int) lateFeeAmount);
                    fee.setRemainingAmount((int) lateFeeAmount);
                    fee.setUpdatedAt(instantUpdateValue);
                }
            }
        }
        feeRepository.saveAll(fees);
    }
}
