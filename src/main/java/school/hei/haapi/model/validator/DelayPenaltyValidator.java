package school.hei.haapi.model.validator;

import org.springframework.stereotype.Component;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.HashSet;
import java.util.Set;

import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class DelayPenaltyValidator implements Consumer<DelayPenalty> {
    @Override
    public void accept(DelayPenalty delayPenalty) {
    Set<String> violationMessages = new HashSet<>();
    if (delayPenalty.getInterestPercent() < 0 || delayPenalty.getInterestPercent() > 100) {
        violationMessages.add("Interest percent must be between 0 and 100");
    }
    if (delayPenalty.getGraceDelay() < 0) {
        violationMessages.add("Grace delay must be positive");
    }
    if (delayPenalty.getApplicabilityDelayAfterGrace() < 0) {
        violationMessages.add("Applicability delay after grace must be positive");
    }
    if (!violationMessages.isEmpty()) {
        String formattedViolationMessages = violationMessages.stream()
                .map(String::toString)
                .collect(Collectors.joining(". "));
        throw new BadRequestException(formattedViolationMessages);
        }
    }
}
