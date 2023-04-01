package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.model.DelayPenalty;

@Component
public class DelayPenaltyMapper {
    public school.hei.haapi.endpoint.rest.model.DelayPenalty toRest(DelayPenalty domain) {
        return new school.hei.haapi.endpoint.rest.model.DelayPenalty()
                .id(domain.getId())
                .interestPercent(domain.getInterestPercent())
                .interestTimerate(domain.getInterestTimerate())
                .creationDatetime(domain.getCreationDatetime())
                .graceDelay(domain.getGraceDelay())
                .applicabilityDelayAfterGrace(domain.getApplicabilityDelayAfterGrace());
    }
}
