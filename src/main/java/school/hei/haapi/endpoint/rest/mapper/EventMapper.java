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
        restEvent.setStartingTime(event.getStartingTime());
        restEvent.setEndingTime(event.getEndingTime());
        restEvent.setResponsible(event.getResponsible());
        restEvent.setPlace(event.getPlace());
        return restEvent;
    }
    public Event toDomain(Event restEvent){
        return Event.builder()
                .id(restEvent.getId())
                .name(restEvent.getName())
                .startingTime(restEvent.getStartingTime())
                .endingTime(restEvent.getEndingTime())
                .responsible(restEvent.getResponsible())
                .place(restEvent.getPlace())
                .build();
    }
}
