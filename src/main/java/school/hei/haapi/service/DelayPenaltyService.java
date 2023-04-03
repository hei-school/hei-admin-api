package school.hei.haapi.service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.DelayPenaltyRepository;
import school.hei.haapi.repository.FeeRepository;

import java.util.List;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    private final DelayPenaltyRepository delayPenaltyRepository;
    private final FeeRepository feeRepository;
    private final UserRepository userRepository;


    public DelayPenalty getMostRecentDelayPenalty() {
        List<DelayPenalty> delayPenalties = delayPenaltyRepository.findMostRecent(PageRequest.of(0,1));
        return delayPenalties.get(0);
    }
    public DelayPenalty crupdateDelayPenalty(DelayPenalty toCrupdate){
        List<User> users = userRepository.findAll();
        users.forEach(user -> {
            if(user.getProperGraceDelay() == null || user.getProperGraceDelay() == 0){
                user.setProperGraceDelay(0);
            }
            Integer graceDelay = user.getProperGraceDelay() + toCrupdate.getGraceDelay();
            user.setProperGraceDelay(graceDelay);
            userRepository.save(user);
        });
        DelayPenalty delayPenalty = delayPenaltyRepository.save(toCrupdate);
        this.check_and_updated_late_fees();
        return delayPenalty;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void check_and_updated_late_fees() {
        DelayPenalty toCrupdate = getMostRecentDelayPenalty();
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
                    Instant.now().isAfter(lastDayApplicability) &&
                    fee.getRemainingAmount() > 0){
                    long passDays = dueDateTimeWithGraceDelay.until(Instant.now(),
                        ChronoUnit.DAYS) +1;
                    int remainingAmount =
                        (int) (fee.getRemainingAmount() * Math.pow(1 + ((double) toCrupdate.getInterestPercent() / 100), passDays));
                    fee.setRemainingAmount(remainingAmount);
                    fees.add(fee);
                }
            }
        );
        feeRepository.saveAll(lateFees);
    }
}
