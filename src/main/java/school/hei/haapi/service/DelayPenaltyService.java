package school.hei.haapi.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Fee;
import school.hei.haapi.repository.DelayPenaltyRepository;
import school.hei.haapi.repository.FeeRepository;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    private final DelayPenaltyRepository delayPenaltyRepository;
    private final FeeRepository feeRepository;

    public DelayPenalty getCurrentDelayPenalty() {
        List<DelayPenalty> delayPenalties = delayPenaltyRepository.findAll();
        return delayPenalties.get(delayPenalties.size() -1 );
    }
    public DelayPenalty crupdateDelayPenalty(DelayPenalty toCrupdate){
        DelayPenalty delayPenalty = delayPenaltyRepository.save(toCrupdate);
        this.check_and_updated_late_fees();
        return delayPenalty;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void check_and_updated_late_fees() {
        DelayPenalty toCrupdate = getCurrentDelayPenalty();
        List<Fee> lateFees = feeRepository.getFeesByStatus(
            school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE);
        Duration graceDelay = Duration.ofDays(toCrupdate.getGraceDelay());
        Duration applicability = Duration.ofDays(toCrupdate.getApplicabilityDelayAfterGrace());
        ArrayList<Fee> fees = new ArrayList<>();
        lateFees.forEach(
            fee -> {
//                L'intérêt ne devrait etre appliqué qu'après cette date
                Instant dueDateTimeWithGraceDelay  = fee.getDueDatetime().plus(graceDelay);
//                After this the interest percent go back to 0
                Instant lastDayApplicability = dueDateTimeWithGraceDelay.plus(applicability);
                if(Instant.now().isAfter(dueDateTimeWithGraceDelay) &&
                    Instant.now().isBefore(lastDayApplicability) &&
                    fee.getRemainingAmount() > 0){
                    int interestPercent = toCrupdate.getInterestPercent() / 100;
                    int remainingAmount =
                        fee.getRemainingAmount() + (interestPercent * fee.getRemainingAmount());
                    fee.setRemainingAmount(remainingAmount);
                    fees.add(fee);
                }
            }
        );
        feeRepository.saveAll(lateFees);
    }
}
