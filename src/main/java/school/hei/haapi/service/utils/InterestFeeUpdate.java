package school.hei.haapi.service.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Fee;
import school.hei.haapi.repository.FeeRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;

@Component
@AllArgsConstructor
public class InterestFeeUpdate {
    private final FeeRepository feeRepository;

    public void updateInterestFees(DelayPenalty delayPenalty) {
        List<Fee> fees = new ArrayList<>();
        feeRepository.findAll().forEach(fee -> {
            if (this.updateFeeInterest(delayPenalty, fee) != null) {
                fees.add(fee);
            }
        });
        if (fees.size() > 0) {
            feeRepository.saveAll(fees);
        }
    }

    public Fee updateFeeInterest(DelayPenalty delayPenalty, Fee fee) {
        if (fee.getStatus().equals(LATE)) {
            long NOW = Instant.now().getEpochSecond() / 86400;
            long firstDayOfInterestApplication = (fee.getDueDatetime().getEpochSecond() / 86400) + delayPenalty.getGraceDelay();
            long lastDayOfInterestApplication = firstDayOfInterestApplication + delayPenalty.getApplicabilityDelayAfterGrace();
            if (NOW > firstDayOfInterestApplication && NOW <= lastDayOfInterestApplication) {
                long numberOfDayToApplyInterest = NOW - firstDayOfInterestApplication;
                fee.setInterest(this.calculateInterest(
                        fee.getTotalAmount(),
                        delayPenalty.getInterestPercent(),
                        numberOfDayToApplyInterest));
            } else if (NOW > lastDayOfInterestApplication) {
                fee.setInterest(this.calculateInterest(
                        fee.getTotalAmount(),
                        delayPenalty.getInterestPercent(),
                        delayPenalty.getApplicabilityDelayAfterGrace()));
            }
            return fee;
        }
        return null;
    }

    private int calculateInterest(double baseAmount, int interestPercent, long numberOfDays) {
        double interest = 0;
        for (int i = 0; i < numberOfDays; i++) {
            interest += baseAmount * interestPercent / 100;
            baseAmount += interest;
        }
        return (int) interest;
    }
}
