package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.model.Event;

@Component
public class EventMapper {

    public Event toRest(Event event) {
        Event restEvent = new Event();
        restEvent.setId(event.getId());
        restEvent.setEventType(event.getEventType());
        restEvent.setDescription(event.getDescription());
        restEvent.setResponsible(event.getResponsible());
        restEvent.setStart(event.getStart());
        restEvent.setEnd(event.getEnd());
        restEvent.setStatus(event.getStatus());
        restEvent.setPlace(event.getPlace());
        return restEvent;
    }

    public Event toDomain(Event event) {
        return Event.builder()
                .id(event.getId())
                .eventType(Event.EventTypeEnum.valueOf(event.getEventType().toString()))
                .description(event.getDescription())
                .responsible(event.getResponsible())
                .start(event.getStart())
                .end(event.getEnd())
                .place(event.getPlace())
                .status(Event.StatusEnum.valueOf(event.getStatus().toString()))
                .build();
    }

}
