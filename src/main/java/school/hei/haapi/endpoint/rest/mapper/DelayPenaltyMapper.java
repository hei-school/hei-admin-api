package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
  public school.hei.haapi.model.DelayPenalty toDomain(CreateDelayPenaltyChange rest) {
    return school.hei.haapi.model.DelayPenalty.builder()
        .graceDelay(rest.getGraceDelay())
        .interestPercent(rest.getInterestPercent())
        .interestTimerate(rest.getInterestTimerate())
        .applicabilityDelayAfterGrace(rest.getApplicabilityDelayAfterGrace())
        .build();
  }

  public DelayPenalty toRest(school.hei.haapi.model.DelayPenalty domain) {
    return new DelayPenalty()
        .id(domain.getId())
        .graceDelay(domain.getGraceDelay())
        .creationDatetime(domain.getCreationDatetime())
        .interestPercent(domain.getInterestPercent())
        .interestTimerate(domain.getInterestTimerate())
        .applicabilityDelayAfterGrace(domain.getApplicabilityDelayAfterGrace());
  }
}
