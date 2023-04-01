package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class DelayPenaltyValidator implements Consumer<CreateDelayPenaltyChange> {

  @Override
  public void accept(CreateDelayPenaltyChange delayPenalty) {
    Set<String> violationMessages = new HashSet<>();
    if (delayPenalty.getInterestPercent() == null) {
      violationMessages.add("Interest percent is mandatory");
    }
    if (delayPenalty.getGraceDelay() == null) {
      violationMessages.add("Grace delay is mandatory");
    }
    if (delayPenalty.getApplicabilityDelayAfterGrace() == null) {
      violationMessages.add("Applicability delay is mandatory");
    }
    if (delayPenalty.getInterestTimerate() == null) {
      violationMessages.add("Interest timerate is mandatory");
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages = violationMessages.stream()
          .map(String::toString)
          .collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
