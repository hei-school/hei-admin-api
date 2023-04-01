package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
    private final DelayPenaltyService service;

    public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty delayPenalty){
        return new DelayPenalty()
                .id(delayPenalty.getId())
                .interestPercent(delayPenalty.getInterestPercent())
                .graceDelay(delayPenalty.getGraceDelay())
                .applicabilityDelayAfterGrace(delayPenalty.getApplicabilityDelayAfterGrace())
                .creationDatetime(delayPenalty.getCreationDatetime())
                .interestTimerate(delayPenalty.getInterestTimerateEnum());
    }
}
