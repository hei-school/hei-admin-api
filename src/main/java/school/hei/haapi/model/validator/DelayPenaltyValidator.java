package school.hei.haapi.model.validator;

import org.springframework.stereotype.Component;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class DelayPenaltyValidator implements Consumer<DelayPenalty> {

    public void accept(List<DelayPenalty> DelayPenalties) {
        DelayPenalties.forEach(this::accept);
    }

    @Override
    public void accept(DelayPenalty delayPenalty) {
        Set<String> violationMessages = new HashSet<>();
        if (delayPenalty.getApplicabilityDelayAfterGrace() < 0) {
            violationMessages.add("Applicability Delay After Grace must be positive");
        }
        if (delayPenalty.getInterestPercent() < 0) {
            violationMessages.add("Interest Percent must be positive");
        }
        if (delayPenalty.getGraceDelay() < 0) {
            violationMessages.add("Grace Dela must be positive");
        }
        if (!violationMessages.isEmpty()) {
            String formattedViolationMessages = violationMessages.stream()
                    .map(String::toString)
                    .collect(Collectors.joining(". "));
            throw new BadRequestException(formattedViolationMessages);
        }
    }
}
