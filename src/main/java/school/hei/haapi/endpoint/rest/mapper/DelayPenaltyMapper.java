package school.hei.haapi.endpoint.rest.mapper;

import java.time.Instant;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.DelayPenalty.InterestTimerate;

@Component
public class DelayPenaltyMapper {
  public school.hei.haapi.endpoint.rest.model.DelayPenalty toRest(DelayPenalty domain) {
    return new school.hei.haapi.endpoint.rest.model.DelayPenalty()
        .id(domain.getId())
        .creationDatetime(domain.getCreationDatetime())
        .graceDelay(domain.getGraceDelay())
        .interestPercent(domain.getInterestPercent())
        .applicabilityDelayAfterGrace(domain.getApplicabilityDelayAfterGrace())
        .interestTimerate(
            InterestTimerateEnum.fromValue(domain.getInterestTimerate().toString()));
  }

  public school.hei.haapi.model.DelayPenalty toDomain(CreateDelayPenaltyChange rest) {
    return school.hei.haapi.model.DelayPenalty.builder()
        .graceDelay(rest.getGraceDelay())
        .applicabilityDelayAfterGrace(rest.getApplicabilityDelayAfterGrace())
        .interestPercent(rest.getInterestPercent())
        .interestTimerate(InterestTimerate.valueOf(rest.getInterestTimerate().toString()))
        .build();
  }
}

