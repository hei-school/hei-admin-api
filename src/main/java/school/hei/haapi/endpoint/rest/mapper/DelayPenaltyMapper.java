package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
    private final DelayPenaltyService service;

    public DelayPenalty ToRest(DelayPenalty delayPenalty){
        return DelayPenalty.builder()
                .id(delayPenalty.getId())
                .interestPercent(delayPenalty.getInterestPercent())
                .interestTimerateEnum(delayPenalty.getInterestTimerateEnum())
                .graceDelay(delayPenalty.getGraceDelay())
                .applicabilityDelayAfterGrace(delayPenalty.getApplicabilityDelayAfterGrace())
                .creationDatetime(delayPenalty.getCreationDatetime())
                .build();
    }
}
