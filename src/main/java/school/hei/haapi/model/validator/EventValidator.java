package school.hei.haapi.model.validator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.exception.BadRequestException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class EventValidator implements Consumer<Event> {
    public final Validator validator;

    public void accept(List<Event> events) {
        events.forEach(this::accept);
    }

    @Override
    public void accept(Event event) {
        Set<ConstraintViolation<Event>> violations = validator.validate(event);
        if (!violations.isEmpty()){
            String constraintMessages = violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(". "));
            throw new BadRequestException(constraintMessages);
        }
    }
}
