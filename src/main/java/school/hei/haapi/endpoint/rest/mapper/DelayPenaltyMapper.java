package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
    public school.hei.haapi.model.DelayPenalty toDomain(DelayPenalty delayPenalty) {
        int graceDelay = delayPenalty.getGraceDelay() != null ? delayPenalty.getGraceDelay() : 0;
        int applicabilityDelayAfterGrace = delayPenalty.getApplicabilityDelayAfterGrace()!=null? delayPenalty.getGraceDelay(): 0;

        return school.hei.haapi.model.DelayPenalty.builder()
                .id(delayPenalty.getId())
                .interestTimerate(delayPenalty.getInterestTimerate().getValue())
                .creationDatetime(delayPenalty.getCreationDatetime())
                .graceDelay(graceDelay)
                .applicabilityDelayAfterGrace(applicabilityDelayAfterGrace)
                .build();
    }

    public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty delayPenalty) {
        return new DelayPenalty()
                .id(delayPenalty.getId())
                .interestTimerate(DelayPenalty.InterestTimerateEnum.valueOf(delayPenalty.getInterestTimerate()))
                .creationDatetime(delayPenalty.getCreationDatetime())
                .graceDelay(delayPenalty.getGraceDelay())
                .applicabilityDelayAfterGrace(delayPenalty.getApplicabilityDelayAfterGrace());
    }
}
