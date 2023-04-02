package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
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

  public school.hei.haapi.model.DelayPenalty toDomain(CreateDelayPenaltyChange rest) {
    DelayPenalty.InterestTimerateEnum interestTimerateEnum =
        rest.getInterestTimerate() == CreateDelayPenaltyChange.InterestTimerateEnum.DAILY ?
            DelayPenalty.InterestTimerateEnum.DAILY : null;
    return new school.hei.haapi.model.DelayPenalty().toBuilder()
        .interestPercent(rest.getInterestPercent())
        .interestTimeRate(interestTimerateEnum)
        .graceDelay(rest.getGraceDelay())
        .applicabilityDelayAfterGrace(rest.getApplicabilityDelayAfterGrace())
        .build();
  }
}
