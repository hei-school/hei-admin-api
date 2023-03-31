package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;

@Component
public class DelayPenaltyMapper {
    public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty delayPenalty) {
        return new DelayPenalty()
                .id(delayPenalty.getId())
                .interestPercent(delayPenalty.getInterestPercent())
                .interestTimerate(delayPenalty.getInterestTimerate())
                .graceDelay(delayPenalty.getGraceDelay())
                .applicabilityDelayAfterGrace(delayPenalty.getApplicabilityDelayAfterGrace())
                .creationDatetime(delayPenalty.getCreationDatetime());
    }

    public DelayPenalty toUpdate(school.hei.haapi.model.DelayPenalty toUpdate){
        DelayPenalty newValue = new DelayPenalty();
        newValue.setInterestPercent(toUpdate.getInterestPercent());
        newValue.setInterestTimerate(toUpdate.getInterestTimerate());
        newValue.setGraceDelay(toUpdate.getGraceDelay());
        newValue.setApplicabilityDelayAfterGrace(toUpdate.getApplicabilityDelayAfterGrace());
        return newValue;
    }

    public school.hei.haapi.model.DelayPenalty toDomain(CreateDelayPenaltyChange delayPenaltyChange) {
        school.hei.haapi.model.DelayPenalty delayPenalty = new school.hei.haapi.model.DelayPenalty();
        delayPenalty.setInterestPercent(delayPenaltyChange.getInterestPercent());
        delayPenalty.setInterestTimerate(delayPenalty.getInterestTimerate());
        delayPenalty.setGraceDelay(delayPenaltyChange.getGraceDelay());
        delayPenalty.setApplicabilityDelayAfterGrace(delayPenaltyChange.getApplicabilityDelayAfterGrace());
        return delayPenalty;
    }
}
