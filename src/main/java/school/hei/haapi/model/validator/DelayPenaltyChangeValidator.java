package school.hei.haapi.model.validator;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class DelayPenaltyChangeValidator implements Consumer<CreateDelayPenaltyChange> {
    @Override
    public void accept(CreateDelayPenaltyChange delayPenaltyChange) {
        Set<String> violationMessages = new HashSet<>();

        if (delayPenaltyChange.getGraceDelay() != null && delayPenaltyChange.getGraceDelay() < 0) {
            violationMessages.add("Grace delay must be positive");
        }
        if (delayPenaltyChange.getApplicabilityDelayAfterGrace() != null && delayPenaltyChange.getApplicabilityDelayAfterGrace() < 0) {
            violationMessages.add("Applicability delay after grace must be positive");
        }
        if (delayPenaltyChange.getInterestPercent() != null && delayPenaltyChange.getInterestPercent() < 0) {
            violationMessages.add("Interest percent must be positive");
        }

        if (!violationMessages.isEmpty()) {
            String formattedViolationMessages = violationMessages.stream()
                    .map(String::toString)
                    .collect(Collectors.joining(". "));
            throw new BadRequestException(formattedViolationMessages);
        }
    }
}
