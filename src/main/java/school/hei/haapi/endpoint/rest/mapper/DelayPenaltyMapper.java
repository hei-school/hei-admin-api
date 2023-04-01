package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
    private DelayPenaltyService delayPenaltyService;
    public DelayPenalty toRestDelayPenalty(school.hei.haapi.model.DelayPenalty delayPenalty){
        return 
                new DelayPenalty()
                        .id(delayPenalty.getId())
                        .graceDelay(delayPenalty.getGraceDelay())
                        .creationDatetime(delayPenalty.getCreationDatetime())
                        .applicabilityDelayAfterGrace(delayPenalty.getApplicabilityDelayAfterGrace())
                        .interestTimerate(delayPenalty.getInterestTimerate())
                        .interestPercent(delayPenalty.getInterestPercent());


    }

}
