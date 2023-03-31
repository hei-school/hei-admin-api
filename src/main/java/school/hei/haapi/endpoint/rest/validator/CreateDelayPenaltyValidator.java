package school.hei.haapi.endpoint.rest.validator;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class CreateDelayPenaltyValidator implements Consumer<CreateDelayPenaltyChange> {
    @Override
    public void accept(CreateDelayPenaltyChange delayPenaltyChange) {
        Set<String> violationMessages = new HashSet<>();
        if (delayPenaltyChange.getInterestPercent() == null) {
            violationMessages.add("interest_percent is mandatory");
        }
        if (delayPenaltyChange.getInterestTimerate() == null) {
            violationMessages.add("interest_timerate is mandatory");
        }
        if (delayPenaltyChange.getGraceDelay() == null) {
            violationMessages.add("grace_delay is mandatory");
        }
        if (delayPenaltyChange.getApplicabilityDelayAfterGrace() == null) {
            violationMessages.add("applicability_delay_after_grace is mandatory");
        }
        if (!violationMessages.isEmpty()) {
            String formattedViolationMessages = violationMessages.stream()
                    .map(String::toString)
                    .collect(Collectors.joining(". "));
            throw new BadRequestException(formattedViolationMessages);
        }
    }
}
