package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateEventParticipant;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.service.EventService;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class EventParticipantMapper {
    private final EventService eventService;
    private final UserService userService;

    public EventParticipant toRest(school.hei.haapi.model.EventParticipant eventParticipant){
        return new EventParticipant()
                .id(eventParticipant.getId())
                .status(EventParticipant.StatusEnum.valueOf(eventParticipant.getStatus().toString()))
                .userId(eventParticipant.getUser().getId());
    }

    public school.hei.haapi.model.EventParticipant toDomain(String eventId,CreateEventParticipant createEventParticipant){
        return school.hei.haapi.model.EventParticipant.builder()
                .id(createEventParticipant.getId())
                .event(eventService.getById(eventId))
                .user(userService.getById(createEventParticipant.getUserId()))
                .build();
    }


}
