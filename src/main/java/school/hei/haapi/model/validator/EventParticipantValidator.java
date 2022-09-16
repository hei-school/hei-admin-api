package school.hei.haapi.model.validator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.exception.BadRequestException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class EventParticipantValidator implements Consumer<EventParticipant> {
    private final Validator validator;

    public void accept(List<EventParticipant> eventParticipants) {
        eventParticipants.forEach(this::accept);
    }

    @Override
    public void accept(EventParticipant eventParticipant) {
        Set<ConstraintViolation<EventParticipant>> violations = validator.validate(eventParticipant);
        if (!violations.isEmpty()) {
            String constraintMessages = violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(". "));
            throw new BadRequestException(constraintMessages);
        }
    }
}
