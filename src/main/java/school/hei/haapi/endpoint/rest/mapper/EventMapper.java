package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.User;
import school.hei.haapi.service.PlaceService;
import school.hei.haapi.service.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class EventMapper {

    public PlaceService placeService;
    public UserService userService;
    public school.hei.haapi.endpoint.rest.model.Event toRestEvent(Event event){
        var restEvent = new school.hei.haapi.endpoint.rest.model.Event();
        restEvent.setId(event.getId());
        restEvent.setName(event.getName());
        restEvent.setType(String.valueOf(event.getType()));
        restEvent.setStartEvent(Instant.from(event.getStartEvent()));
        restEvent.setEndEvent(Instant.from(event.getEndEvent()));
        restEvent.setStatus(String.valueOf(event.getStatus()));
        restEvent.setResponsibleId(event.getResponsible().getId());
        restEvent.setPlaceId(event.getPlace().getId());
        return restEvent;
    }
    public Event toDomain(school.hei.haapi.endpoint.rest.model.Event restEvent){
        return Event.builder()
                .id(restEvent.getId())
                .name(restEvent.getName())
                .type(Event.EventType.valueOf(restEvent.getType()))
                .startEvent(restEvent.getStartEvent())
                .endEvent(restEvent.getEndEvent())
                .status(Event.EventStatus.valueOf(restEvent.getStatus()))
                .responsible(userService.getById(restEvent.getResponsibleId()) )
                .place(placeService.getById(restEvent.getPlaceId()))
                .build();
    }
    
}
