package school.hei.haapi.endpoint.rest.mapper;

import java.time.Instant;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.DelayPenaltyService;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
  DelayPenaltyService delayPenaltyService;

  public school.hei.haapi.endpoint.rest.model.DelayPenalty toRest(DelayPenalty delayPenalty) {
    var restDelayPenalty = new school.hei.haapi.endpoint.rest.model.DelayPenalty();
    restDelayPenalty.setId(delayPenalty.getId());
    restDelayPenalty.setCreationDatetime(delayPenalty.getCreationDatetime());
    restDelayPenalty.setInterestTimerate(delayPenalty.getInterestTimeRate());
    restDelayPenalty.setApplicabilityDelayAfterGrace(
        delayPenalty.getApplicabilityDelayAfterGrace());
    restDelayPenalty.setGraceDelay(delayPenalty.getGraceDelay());
    restDelayPenalty.setInterestPercent(delayPenalty.getInterestPercent());
    return restDelayPenalty;
  }

  public DelayPenalty toDomain(CreateDelayPenaltyChange createDelayPenaltyChange) {
    Instant delayPenaltyCreateDateTime = delayPenaltyService.getFirstItem().getCreationDatetime();
    return DelayPenalty.builder()
        .interestPercent(createDelayPenaltyChange.getInterestPercent())
        .creationDatetime(delayPenaltyCreateDateTime)
        .lastUpdateDate(Instant.now())
        .interestTimeRate(toDomainDelayInterest(
            Objects.requireNonNull(createDelayPenaltyChange.getInterestTimerate())))
        .graceDelay(createDelayPenaltyChange.getGraceDelay())
        .applicabilityDelayAfterGrace(createDelayPenaltyChange.getApplicabilityDelayAfterGrace())
        .build();
  }

  private school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum toDomainDelayInterest(
      CreateDelayPenaltyChange.InterestTimerateEnum createDelayPenalty) {
    switch (createDelayPenalty) {
      case DAILY:
        return school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum.DAILY;
      default:
        throw new BadRequestException(
            "Unexpected delayPenaltyInterestTimeRate: " + createDelayPenalty.getValue());
    }
  }
}
