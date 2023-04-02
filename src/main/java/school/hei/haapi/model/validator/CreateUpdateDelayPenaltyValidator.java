package school.hei.haapi.model.validator;

import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange.InterestTimerateEnum;

@Component
public class CreateUpdateDelayPenaltyValidator implements Consumer<CreateDelayPenaltyChange> {
  @Override
  public void accept(CreateDelayPenaltyChange create) {
    if(create.getInterestPercent() <= 0 || create.getInterestPercent() == null) {
      throw new BadRequestException("Interest is mandatory and cannot be negative");
    }
    if(create.getInterestTimerate() != null || !(create.getInterestTimerate() instanceof InterestTimerateEnum)) {
      throw new BadRequestException("Timerate is mandatory InterestTimerateEnum type");
    }
    if(create.getGraceDelay() <= 0 || create.getGraceDelay() == null) {
      throw new BadRequestException("Grace delay is mandatory and cannot be negative");
    }
    if(create.getApplicabilityDelayAfterGrace() <= 0 || create.getApplicabilityDelayAfterGrace() == null) {
      throw new BadRequestException("Applicability after grace is mandatory and cannot be negative");
    }
  }
}
