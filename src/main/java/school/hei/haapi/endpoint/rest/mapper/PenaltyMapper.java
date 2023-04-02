package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

import java.util.Objects;

@Component
@AllArgsConstructor
public class PenaltyMapper {
    private final DelayPenaltyService delayPenaltyService;

    public DelayPenalty toRestDelayPenalty(school.hei.haapi.model.DelayPenalty penalty) {
        DelayPenalty rest = new DelayPenalty();
        rest.setId(penalty.getId());
        rest.setInterestPercent(penalty.getInterestPercent());
        rest.setGraceDelay(penalty.getGraceDelay());
        rest.setApplicabilityDelayAfterGrace(penalty.getApplicabilityDelayAfterGrace());
        rest.setCreationDatetime(penalty.getCreationDateTime());
        rest.setInterestTimerate(DelayPenalty.InterestTimerateEnum.valueOf(penalty.getInterestTimeRate().toString()));
        return rest;
    }

    public school.hei.haapi.model.DelayPenalty toDomain (CreateDelayPenaltyChange createDelayPenaltyChange){
        school.hei.haapi.model.DelayPenalty actual = delayPenaltyService.getActualDelayPenalty();
        return school.hei.haapi.model.DelayPenalty.builder()
                .id(actual.getId())
                .interestPercent(createDelayPenaltyChange.getInterestPercent())
                .InterestTimeRate(school.hei.haapi.model.DelayPenalty.InterestTimerateEnum.valueOf(Objects.requireNonNull(createDelayPenaltyChange.getInterestTimerate()).toString()))
                .applicabilityDelayAfterGrace(createDelayPenaltyChange.getApplicabilityDelayAfterGrace())
                .graceDelay(createDelayPenaltyChange.getGraceDelay())
                .creationDateTime(actual.getCreationDateTime())
                .build();
    }
}

