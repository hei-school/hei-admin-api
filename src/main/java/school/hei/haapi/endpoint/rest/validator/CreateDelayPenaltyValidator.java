package school.hei.haapi.endpoint.rest.validator;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.function.Consumer;

@Component
public class CreateDelayPenaltyValidator implements Consumer<CreateDelayPenaltyChange> {
    @Override
    public void accept(CreateDelayPenaltyChange delayPenalty) {
        if (delayPenalty.getInterestPercent() < 0 || delayPenalty.getInterestPercent() > 100) {
            throw new BadRequestException("The interest percent cannot exceed 100 and cannot be negative");
        }
    }
}