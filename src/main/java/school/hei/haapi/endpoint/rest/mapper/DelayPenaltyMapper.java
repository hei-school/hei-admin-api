package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum;

@Component
public class DelayPenaltyMapper {
  public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty domain) {
    return new DelayPenalty()
        .id(domain.getId())
        .creationDatetime(domain.getCreationDatetime())
        .graceDelay(domain.getGraceDelay())
        .interestPercent(domain.getInterestPercent())
        .applicabilityDelayAfterGrace(domain.getApplicabilityDelayAfterGrace())
        .interestTimerate(
            InterestTimerateEnum.fromValue(domain.getInterestTimerate().toString()));
  }
}
