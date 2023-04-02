package school.hei.haapi.endpoint.rest.validator;


import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.function.Consumer;

@Component
public class UpdateDelayPenaltyValidator implements Consumer<DelayPenalty> {
    @Override public void accept(DelayPenalty delayPenalty) {
        if (delayPenalty.getGraceDelay() == null) {
            throw new BadRequestException("GraceDelay is mandatory");
        }
        if (delayPenalty.getInterestPercent() == null) {
            throw new BadRequestException("Interest Percent is mandatory");
        }
        if (delayPenalty.getApplicabilityDelayAfterGrace() == null) {
            throw new BadRequestException("Applicability Delay after Grace is mandatory");
        }
        if (delayPenalty.getInterestTimerate() == null) {
            throw new BadRequestException("Interest Time rate is mandatory");
        }
        if (delayPenalty.getGraceDelay() < 0) {
            throw new BadRequestException("GraceDelay should be positive");
        }
        if (delayPenalty.getInterestPercent() < 0) {
            throw new BadRequestException("Interest Percent should be positive");
        }
        if (delayPenalty.getApplicabilityDelayAfterGrace() < 0) {
            throw new BadRequestException("Applicability Delay after Grace should be positive");
        }
    }
}
