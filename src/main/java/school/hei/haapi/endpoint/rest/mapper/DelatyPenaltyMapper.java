package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;

@Component
public class DelatyPenaltyMapper {
  public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty domain) {
    return new DelayPenalty()
        .id(domain.getId())
        .interestPercent(domain.getInterestPercent())
        .interestTimerate(domain.getInterestTimeRate())
        .graceDelay(domain.getGraceDelay())
        .applicabilityDelayAfterGrace(domain.getApplicabilityDelayAfterGrace())
        .creationDatetime(domain.getCreationDatetime());
  }
}
