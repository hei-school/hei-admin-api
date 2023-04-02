package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.DelayPenalty;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
    public school.hei.haapi.endpoint.rest.model.DelayPenalty toRest(DelayPenalty delayPenalty) {
        return new school.hei.haapi.endpoint.rest.model.DelayPenalty()
            .id(delayPenalty.getId())
            .creationDatetime(delayPenalty.getCreationDatetime())
            .interestPercent(delayPenalty.getInterestPercent())
            .interestTimerate(delayPenalty.getInterestTimerate())
            .graceDelay(delayPenalty.getGraceDelay())
            .applicabilityDelayAfterGrace(delayPenalty.getApplicabilityDelayAfterGrace());
    }

    public DelayPenalty toDomain(school.hei.haapi.endpoint.rest.model.DelayPenalty restDelayPenalty) {
        return DelayPenalty.builder()
                .id(restDelayPenalty.getId())
                .creationDatetime(restDelayPenalty.getCreationDatetime())
                .interestPercent(restDelayPenalty.getInterestPercent())
                .interestTimerate(restDelayPenalty.getInterestTimerate())
                .graceDelay(restDelayPenalty.getGraceDelay())
                .applicabilityDelayAfterGrace(restDelayPenalty.getApplicabilityDelayAfterGrace())
                .build();
    }
}
