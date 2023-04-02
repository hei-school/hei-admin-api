package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.DelayPenalty;


@Component
public class DelayPenaltyMapper {
    public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty domain){
        return new DelayPenalty()
                .id(domain.getId())
                .interestPercent(domain.setInterestPercent())
                .interestTimerate(domain.getInterestTimeRate())
                .graceDelay(domain.getGraceDelay())
                .applicabilityDelayAFfterGrace(domain.getApplicabilityDelayAfterGrace())
                .creationDateTime(domain.getCreationDatetime());

    }
}
