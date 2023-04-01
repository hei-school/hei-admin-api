package school.hei.haapi.service.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Fee;
import school.hei.haapi.repository.FeeRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;

@Component
@AllArgsConstructor
public class InterestFeeUpdate {
    private final FeeRepository feeRepository;

    public void updateInterestFees(DelayPenalty delayPenalty) {
        feeRepository.findAll().forEach(fee -> this.updateFeeInterest(delayPenalty, fee));
    }

    public void updateFeeInterest(DelayPenalty delayPenalty, Fee fee) {
        if (fee.getStatus().equals(LATE)) {
            if (
                    Instant.now().isAfter(fee.getDueDatetime().plus(delayPenalty.getGraceDelay(), ChronoUnit.DAYS)) &&
                            Instant.now().isBefore(fee.getDueDatetime()
                                    .plus(delayPenalty.getGraceDelay(), ChronoUnit.DAYS)
                                    .plus(delayPenalty.getApplicabilityDelayAfterGrace(), ChronoUnit.DAYS)
                            )
            ) {
                double baseAmount = fee.getTotalAmount();
                int numberOfDays = (int) ChronoUnit.DAYS.between(Instant.now(),
                        fee.getDueDatetime()
                                .plus(delayPenalty.getGraceDelay(), ChronoUnit.DAYS)
                                .plus(delayPenalty.getApplicabilityDelayAfterGrace(), ChronoUnit.DAYS));
                fee.setInterest(this.calculInterest(
                        baseAmount,
                        delayPenalty.getInterestPercent(),
                        numberOfDays));
                feeRepository.save(fee);
            }
        }
    }

    private double calculInterest(double baseAmount, int interestPercent, int numberOfDays) {
        double interest = 0;
        for (int i = 0; i < numberOfDays; i++) {
            interest += baseAmount * interestPercent / 100;
            baseAmount += interest;
        }
        return interest;
    }
}
