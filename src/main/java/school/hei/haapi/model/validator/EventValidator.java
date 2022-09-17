package school.hei.haapi.model.validator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class EventValidator implements Consumer<Event> {
    @Override
    public void accept(Event event) {
        Set<String> violationMessages = new HashSet<>();
        if (event.getName() == null) {
            violationMessages.add("name is mandatory");
        }
        if(event.getRef() == null) {
            violationMessages.add("ref is mandatory");
        }
        if(event.getStartingHours() == null) {
            violationMessages.add("starting hours is mandatory");
        }
        if(event.getEndingHours() == null) {
            violationMessages.add("ending hours is mandatory");
        }
        if(event.getPlace() == null) {
            violationMessages.add("place is mandatory");
        }
        if (!violationMessages.isEmpty()) {
            String formattedViolationMessages = violationMessages.stream()
                    .map(String::toString)
                    .collect(Collectors.joining(". "));
            throw new BadRequestException(formattedViolationMessages);
        }
    }

    public void accept(List<Event> eventList) {
        eventList.forEach(this::accept);
    }
}
