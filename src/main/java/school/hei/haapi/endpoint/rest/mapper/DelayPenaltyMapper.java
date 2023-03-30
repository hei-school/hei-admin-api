package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;

@Component
public class DelayPenaltyMapper {

    public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty delayPenalty){
        var restDelayPenalty = new DelayPenalty();
        restDelayPenalty.setId(delayPenalty.getId());
        restDelayPenalty.setInterestPercent(delayPenalty.getInterest_percent());
        restDelayPenalty.setInterestTimerate(DelayPenalty.InterestTimerateEnum.valueOf(delayPenalty.getInterest_timerate().name()));
        restDelayPenalty.setGraceDelay(delayPenalty.getGrace_delay());
        restDelayPenalty.setApplicabilityDelayAfterGrace(delayPenalty.getApplicability_delay_after_grace());
        restDelayPenalty.setCreationDatetime(delayPenalty.getCreation_datetime());

        return  restDelayPenalty;
    }
}
