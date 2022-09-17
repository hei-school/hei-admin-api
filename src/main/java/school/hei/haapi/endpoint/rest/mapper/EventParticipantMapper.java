package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateEventParticipant;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.UserRepository;

import java.util.Objects;

import static school.hei.haapi.endpoint.rest.model.EventParticipant.StatusEnum.*;

@Component
@AllArgsConstructor
public class EventParticipantMapper {
    private EventRepository eventRepository;
    private UserRepository userRepository;
    private EventParticipantRepository eventParticipantRepository;

    public school.hei.haapi.endpoint.rest.model.EventParticipant toRest(EventParticipant eventParticipant) {
        var restEventParticipant = new school.hei.haapi.endpoint.rest.model.EventParticipant();
        restEventParticipant.setId(eventParticipant.getId());
        restEventParticipant.setStatus(eventParticipant.getStatus());
        restEventParticipant.setUserParticipantId(eventParticipant.getUserParticipant().getId());
        restEventParticipant.setEventId(eventParticipant.getEvent().getId());
        return restEventParticipant;
    }

    public EventParticipant toDomain(school.hei.haapi.endpoint.rest.model.EventParticipant eventParticipant) {
        if (eventParticipant.getUserParticipantId() == null) {
            eventParticipant.setUserParticipantId(eventParticipantRepository.getById(
                    Objects.requireNonNull(eventParticipant.getId())).getUserParticipant().getId()
            );
        }
        if (eventParticipant.getEventId() == null) {
            eventParticipant.setEventId(eventParticipantRepository.getById(
                    Objects.requireNonNull(eventParticipant.getId())).getEvent().getId()
            );
        }
        return EventParticipant.builder()
                .id(eventParticipant.getId())
                .status(eventParticipant.getStatus())
                .userParticipant(userRepository.getById(Objects.requireNonNull(eventParticipant.getUserParticipantId())))
                .event(eventRepository.getById(Objects.requireNonNull(eventParticipant.getEventId())))
                .build();
    }

    public school.hei.haapi.endpoint.rest.model.EventParticipant toRestCreateEventParticipant(String eventId, CreateEventParticipant createEventParticipant) {
        var eventParticipant = new school.hei.haapi.endpoint.rest.model.EventParticipant();
        eventParticipant.setStatus(EXPECTED);
        eventParticipant.setUserParticipantId(createEventParticipant.getUserParticipantId());
        eventParticipant.setEventId(eventId);
        return eventParticipant;
    }
}
