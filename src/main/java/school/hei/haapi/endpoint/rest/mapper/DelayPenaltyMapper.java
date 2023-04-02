package school.hei.haapi.endpoint.rest.mapper;

import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.exception.ApiException;

@Component
@AllArgsConstructor
public class DelayPenaltyMapper {
    public school.hei.haapi.endpoint.rest.model.DelayPenalty toRest(DelayPenalty delayPenalty) {
        return new school.hei.haapi.endpoint.rest.model.DelayPenalty()
            .id(delayPenalty.getId())
            .creationDatetime(delayPenalty.getCreationDatetime())
            .interestPercent(delayPenalty.getInterestPercent())
            .interestTimerate(delayPenalty.getInterestTimerate())
            .graceDelay(delayPenalty.getGraceDelay())
            .applicabilityDelayAfterGrace(delayPenalty.getApplicabilityDelayAfterGrace());
    }

    public DelayPenalty toDomain(school.hei.haapi.endpoint.rest.model.DelayPenalty restDelayPenalty) {
        return DelayPenalty.builder()
                .id(restDelayPenalty.getId())
                .creationDatetime(restDelayPenalty.getCreationDatetime())
                .interestPercent(restDelayPenalty.getInterestPercent())
                .interestTimerate(restDelayPenalty.getInterestTimerate())
                .graceDelay(restDelayPenalty.getGraceDelay())
                .applicabilityDelayAfterGrace(restDelayPenalty.getApplicabilityDelayAfterGrace())
                .build();
    }

  public school.hei.haapi.model.DelayPenalty toDomain(CreateDelayPenaltyChange rest){
    return school.hei.haapi.model.DelayPenalty.builder()
        .interestPercent(rest.getInterestPercent())
        .interestTimerate(convertType(rest.getInterestTimerate()))
        .applicabilityDelayAfterGrace(rest.getApplicabilityDelayAfterGrace())
        .graceDelay(rest.getGraceDelay())
        .creationDatetime(Instant.now())
        .build();
  }

  private school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum convertType(
      CreateDelayPenaltyChange.InterestTimerateEnum type) {
    if (type == null) {
      return null;
    }
    switch (type.getValue()) {
      case "DAILY":
        return school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum.DAILY;
      default:
        throw new ApiException(ApiException.ExceptionType.SERVER_EXCEPTION,
            "Interest Timerate: " + type.getValue() + " not found");
    }
  }
}
