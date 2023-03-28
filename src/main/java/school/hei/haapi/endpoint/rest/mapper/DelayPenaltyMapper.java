package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;

@Component
public class DelayPenaltyMapper {
    public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty delayPenalty) {
        return new DelayPenalty()
                .id(delayPenalty.getId())
                .interestPercent(delayPenalty.getInterestPercent())
                .interestTimerate(delayPenalty.getInterestTimerate())
                .graceDelay(delayPenalty.getGraceDelay())
                .applicabilityDelayAfterGrace(delayPenalty.getApplicabilityDelayAfterGrace())
                .creationDatetime(delayPenalty.getCreationDatetime());
    }
}
