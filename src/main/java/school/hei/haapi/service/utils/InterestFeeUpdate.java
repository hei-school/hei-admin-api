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
        feeRepository.getFeesByStatus(LATE).forEach(fee -> {
            if (this.updateFeeInterest(delayPenalty, fee) != null) {
                fees.add(fee);
            }
        });
        if (fees.size() > 0) {
            feeRepository.saveAll(fees);
        }
    }

    public Fee updateFeeInterest(DelayPenalty delayPenalty, Fee fee) {
        long NOW = Instant.now().getEpochSecond() / 86400;
        long firstDayOfInterestApplication = (fee.getDueDatetime().getEpochSecond() / 86400) + (
                fee.getStudent().getGraceDelay() > 0 ? fee.getStudent().getGraceDelay() : delayPenalty.getGraceDelay());
        long lastDayOfInterestApplication = firstDayOfInterestApplication + delayPenalty.getApplicabilityDelayAfterGrace();
        if (NOW > firstDayOfInterestApplication && NOW <= lastDayOfInterestApplication) {
            long numberOfDayToApplyInterest = NOW - firstDayOfInterestApplication;
            fee.setInterest(this.calculateInterest(
                    fee.getRemainingAmount(),
                    delayPenalty.getInterestPercent(),
                    numberOfDayToApplyInterest));
            return fee;
        }
        return null;
//        IDEA: if it is also necessary to update the fees that have already extended the deadline
//        else if (NOW > lastDayOfInterestApplication) {
//            fee.setInterest(this.calculateInterest(
//                    fee.getRemainingAmount(),
//                    delayPenalty.getInterestPercent(),
//                    delayPenalty.getApplicabilityDelayAfterGrace()));
//            return fee;
//        }
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
