package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
    private final DelayPenaltyService service;

    public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty penalty){
        return new DelayPenalty()
                .id(penalty.getId())
                .creationDatetime(penalty.getCreationDateTime())
                .graceDelay(penalty.getGraceDelay())
                .interestPercent(penalty.getInterestPercent())
                .interestTimerate(penalty.getInterestTimeRate())
                .applicabilityDelayAfterGrace(penalty.getApplicabilityDelayAfterGrace());
    }
}
