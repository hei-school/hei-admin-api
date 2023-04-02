package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum;
import school.hei.haapi.model.validator.CreateUpdateDelayPenaltyValidator;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
    private final CreateUpdateDelayPenaltyValidator delayPenaltyValidator;
    public school.hei.haapi.endpoint.rest.model.DelayPenalty toRest(DelayPenalty domain) {
        return new school.hei.haapi.endpoint.rest.model.DelayPenalty()
                .id(domain.getId())
                .interestPercent(domain.getInterestPercent())
                .interestTimerate(domain.getInterestTimerate())
                .creationDatetime(domain.getCreationDatetime())
                .graceDelay(domain.getGraceDelay())
                .applicabilityDelayAfterGrace(domain.getApplicabilityDelayAfterGrace());
    }

    public DelayPenalty toDomainDelayPenalty(CreateDelayPenaltyChange rest) {
        delayPenaltyValidator.accept(rest);
        return DelayPenalty.builder()
                .interestPercent(rest.getInterestPercent())
                .interestTimerate(InterestTimerateEnum.valueOf(rest.getInterestTimerate().toString()))
                .graceDelay(rest.getGraceDelay())
                .applicabilityDelayAfterGrace(rest.getApplicabilityDelayAfterGrace())
                .build();
    }


}
