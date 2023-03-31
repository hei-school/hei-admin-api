package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;

@Component
public class DelayPenaltyMapper {

    public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty restDelayPenality) {
        var delay = new school.hei.haapi.endpoint.rest.model.DelayPenalty();
        delay.setId(restDelayPenality.getId());
        delay.setInterestPercent(restDelayPenality.getInterest_percent());
        delay.setInterestTimerate(restDelayPenality.getInterest_timerate());
        delay.setGraceDelay(restDelayPenality.getGrace_delay());
        delay.setApplicabilityDelayAfterGrace(restDelayPenality.getApplicability_delay_after_grace());
        delay.setCreationDatetime(restDelayPenality.getCreation_datetime());
        return delay;
    }
}