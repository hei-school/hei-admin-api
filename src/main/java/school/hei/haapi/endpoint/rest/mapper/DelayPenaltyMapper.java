package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.DelayPenalty;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
    public static school.hei.haapi.endpoint.rest.model.DelayPenalty toRest(DelayPenalty delayPenalty) {
        var restDelayPenalty = new school.hei.haapi.endpoint.rest.model.DelayPenalty();
        restDelayPenalty.setId(delayPenalty.getId());
        restDelayPenalty.setCreationDatetime(delayPenalty.getCreationDatetime());
        restDelayPenalty.setInterestPercent(delayPenalty.getInterestPercent());
        restDelayPenalty.setInterestTimerate(delayPenalty.getInterestTimeRate());
        restDelayPenalty.setGraceDelay(delayPenalty.getGraceDelay());
        restDelayPenalty.setApplicabilityDelayAfterGrace(delayPenalty.getApplicabilityDelayAfterGrace());
        return restDelayPenalty;
    }

    public DelayPenalty toDomain(school.hei.haapi.endpoint.rest.model.DelayPenalty restDelayPenalty) {
        return DelayPenalty.builder()
                .id(restDelayPenalty.getId())
                .creationDatetime(restDelayPenalty.getCreationDatetime())
                .interestPercent(restDelayPenalty.getInterestPercent())
                .interestTimeRate(restDelayPenalty.getInterestTimerate())
                .graceDelay(restDelayPenalty.getGraceDelay())
                .applicabilityDelayAfterGrace(restDelayPenalty.getApplicabilityDelayAfterGrace())
                .build();
    }
}
