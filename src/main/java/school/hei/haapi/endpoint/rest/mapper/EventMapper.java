package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Event;

@Component
@AllArgsConstructor
public class EventMapper {
    public Event toRest(Event event){
        var restEvent = new Event();
        restEvent.setId(event.getId());
        restEvent.setName(event.getName());
        restEvent.setEventType(event.getEventType());
        restEvent.setStartingTime(event.getStartingTime());
        restEvent.setEndingTime(event.getEndingTime());
        restEvent.setSupervisor(event.getSupervisor());
        restEvent.setPlace(event.getPlace());
        return restEvent;
    }
    public Event toDomain(Event restEvent){
        return Event.builder()
                .id(restEvent.getId())
                .name(restEvent.getName())
                .eventType(restEvent.getEventType())
                .startingTime(restEvent.getStartingTime())
                .endingTime(restEvent.getEndingTime())
                .supervisor(restEvent.getSupervisor())
                .place(restEvent.getPlace())
                .build();
    }
}
