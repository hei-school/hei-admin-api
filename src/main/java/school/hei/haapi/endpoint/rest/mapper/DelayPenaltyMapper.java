package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.validator.CreateDelayPenaltyValidator;

import java.time.Instant;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
    private final CreateDelayPenaltyValidator validator;
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

    public school.hei.haapi.model.DelayPenalty toDomain(CreateDelayPenaltyChange createDelayPenaltyChange){
        validator.accept(createDelayPenaltyChange);
        return school.hei.haapi.model.DelayPenalty.builder()
                .grace_delay(createDelayPenaltyChange.getGraceDelay())
                .applicability_delay_after_grace(createDelayPenaltyChange.getApplicabilityDelayAfterGrace())
                .creation_datetime(Instant.now())
                .interest_timerate(DelayPenalty.InterestTimerateEnum.valueOf(createDelayPenaltyChange.getInterestTimerate().toString()))
                .interest_percent(createDelayPenaltyChange.getInterestPercent())
                .build();
    }
}