package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;

@Component
public class DelayPenaltyMapper {

    public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty delayPenalty) {
        var delay = new school.hei.haapi.endpoint.rest.model.DelayPenalty();
        delay.setId(delayPenalty.getId());
        delay.setInterestPercent(delayPenalty.getInterestPercent());
        delay.setInterestTimerate(delayPenalty.getInterestTimerate());
        delay.setGraceDelay(delayPenalty.getGraceDelay());
        delay.setApplicabilityDelayAfterGrace(delayPenalty.getApplicabilityDelayAfterGrace());
        delay.setCreationDatetime(delayPenalty.getCreationDatetime());
        return delay;
    }

    public school.hei.haapi.model.DelayPenalty toDomain(DelayPenalty restDelayPenalty) {
        return school.hei.haapi.model.DelayPenalty.builder()
                .id(restDelayPenalty.getId())
                .interestPercent(restDelayPenalty.getInterestPercent())
                .interestTimerate(restDelayPenalty.getInterestTimerate())
                .graceDelay(restDelayPenalty.getGraceDelay())
                .applicabilityDelayAfterGrace(restDelayPenalty.getApplicabilityDelayAfterGrace())
                .creationDatetime(restDelayPenalty.getCreationDatetime())
                .build();
    }
}