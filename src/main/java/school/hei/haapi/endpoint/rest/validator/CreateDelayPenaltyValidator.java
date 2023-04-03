package school.hei.haapi.endpoint.rest.validator;


import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.function.Consumer;

@Component
public class CreateDelayPenaltyValidator implements Consumer<CreateDelayPenaltyChange> {
    @Override public void accept(CreateDelayPenaltyChange createDelayPenaltyChange) {
        if (createDelayPenaltyChange.getGraceDelay() == null) {
            throw new BadRequestException("GraceDelay is mandatory");
        }
        if (createDelayPenaltyChange.getInterestPercent() == null) {
            throw new BadRequestException("Interest Percent is mandatory");
        }
        if (createDelayPenaltyChange.getApplicabilityDelayAfterGrace() == null) {
            throw new BadRequestException("Applicability Delay after Grace is mandatory");
        }
        if (createDelayPenaltyChange.getInterestTimerate() == null) {
            throw new BadRequestException("Interest Time rate is mandatory");
        }
        if (createDelayPenaltyChange.getGraceDelay() < 0) {
            throw new BadRequestException("GraceDelay should be positive");
        }
        if (createDelayPenaltyChange.getInterestPercent() < 0) {
            throw new BadRequestException("Interest Percent should be positive");
        }
        if (createDelayPenaltyChange.getApplicabilityDelayAfterGrace() < 0) {
            throw new BadRequestException("Applicability Delay after Grace should be positive");
        }
    }
}