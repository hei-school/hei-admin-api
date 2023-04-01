package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.validator.CreateDelayPenaltyValidator;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.DelayPenaltyService;

import java.time.Instant;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
  DelayPenaltyService delayPenaltyService;
  CreateDelayPenaltyValidator createDelayPenaltyValidator;

  public school.hei.haapi.endpoint.rest.model.DelayPenalty toRest(DelayPenalty delayPenalty) {
    var restDelayPenalty = new school.hei.haapi.endpoint.rest.model.DelayPenalty();
    restDelayPenalty.setId(delayPenalty.getId());
    restDelayPenalty.setCreationDatetime(delayPenalty.getCreationDatetime());
    restDelayPenalty.setInterestTimerate(delayPenalty.getInterestTimeRate());
    restDelayPenalty.setApplicabilityDelayAfterGrace(delayPenalty.getApplicabilityDelayAfterGrace());
    restDelayPenalty.setGraceDelay(delayPenalty.getGraceDelay());
    restDelayPenalty.setInterestPercent(delayPenalty.getInterestPercent());
    return restDelayPenalty;
  }

  public DelayPenalty toDomain(CreateDelayPenaltyChange createDelayPenaltyChange) {
    createDelayPenaltyValidator.accept(createDelayPenaltyChange);
    DelayPenalty lastDelayPenalty = delayPenaltyService.getLastUpdated();
    return DelayPenalty.builder()
        .id(lastDelayPenalty.getId())
        .interestPercent(createDelayPenaltyChange.getInterestPercent())
        .creationDatetime(lastDelayPenalty.getCreationDatetime())
        .lastUpdateDate(Instant.now())
        .interestTimeRate(toDomainDelayInterest(createDelayPenaltyChange.getInterestTimerate()))
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
