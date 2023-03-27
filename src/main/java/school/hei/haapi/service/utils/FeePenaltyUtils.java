package school.hei.haapi.service.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Fee;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class FeePenaltyUtils {

    public static Fee penalizeFee(Fee lateFee, DelayPenalty delayPenalty) {
        long lateDays = ChronoUnit.DAYS.between(lateFee.getDueDatetime(), Instant.now());
        if (lateDays <= delayPenalty.getGraceDelay()) {
            return lateFee;
        }

        double interest = 0;
        lateDays -= delayPenalty.getGraceDelay();
        if (lateDays > delayPenalty.getApplicabilityDelayAfterGrace()) {
            lateDays = delayPenalty.getApplicabilityDelayAfterGrace();
        }
        for (int i = 0; i < lateDays; i++) {
            interest += CalculusUtils.getPercentage(
                    interest + lateFee.getRemainingAmount(),
                    delayPenalty.getInterestPercent()
            );
        }
        lateFee.setInterest(interest);
        return lateFee;
    }

    public static List<Fee> penalizeFees(List<Fee> lateFees, DelayPenalty delayPenalty) {
        return lateFees.stream().map(fee -> {
            if(fee.getStudent().getGraceDelay() != null){
                delayPenalty.setGraceDelay(fee.getStudent().getGraceDelay());
            }
            return penalizeFee(fee, delayPenalty);
        }).collect(Collectors.toList());
    }
}
