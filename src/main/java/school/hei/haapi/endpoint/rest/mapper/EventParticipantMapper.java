package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.User;
import school.hei.haapi.service.EventService;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class EventParticipantMapper {

    private final UserService userService;
    private final EventService eventService;

    public EventParticipant toDomain(school.hei.haapi.endpoint.rest.model.EventParticipant rest, String eventId){

        User user = userService.findById(rest.getId());
        Event event = eventService.findEventById(eventId);

        return EventParticipant.builder()
                .id(rest.getId())
                .status(rest.getEventStatus())
                .participant(user)
                .event(event)
                .build();
    }

    public school.hei.haapi.endpoint.rest.model.EventParticipant toRest(EventParticipant domain){
        User participant = domain.getParticipant();
        return new school.hei.haapi.endpoint.rest.model.EventParticipant()
                .email(participant.getEmail())
                .eventStatus(domain.getStatus())
                .id(domain.getId())
                .nic(participant.getNic())
                .ref(participant.getRef())
                .firstName(participant.getFirstName())
                .lastName(participant.getLastName());
    }

}
