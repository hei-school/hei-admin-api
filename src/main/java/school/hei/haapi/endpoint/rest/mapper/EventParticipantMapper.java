package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateEventParticipant;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.EventService;
import school.hei.haapi.service.UserService;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toUnmodifiableList;


@Component
@AllArgsConstructor
public class EventParticipantMapper {

    public EventService eventService;
    public UserService userService;
    public EventParticipant toRest(school.hei.haapi.model.EventParticipant eventParticipant){
        return new school.hei.haapi.endpoint.rest.model.EventParticipant()
                .id(eventParticipant.getId())
                .participantId(eventParticipant.getParticipant().getId())
                .eventId(eventParticipant.getEvent().getId())
                .status(eventParticipant.getStatus());
    }

    public school.hei.haapi.model.EventParticipant toDomain(Event event, CreateEventParticipant createEventParticipant){
        //createEventParticipant.accept(createEventParticipant);
        return school.hei.haapi.model.EventParticipant.builder()
                .event(event)
                .participant(userService.getById(createEventParticipant.getParticipantId()))
                .status(toDomainEventParticipantStatus(Objects.requireNonNull(createEventParticipant.getStatus())))
                .build();
    }

    public List<school.hei.haapi.model.EventParticipant> toDomain(String eventId, List<CreateEventParticipant> toCreate){
        Event event = eventService.getById(eventId);
        if (event == null) {
            throw new NotFoundException("event.id=" + eventId + " is not found");
        }
        return toCreate
                .stream()
                .map(createEventParticipant -> toDomain(event, createEventParticipant))
                .collect(toUnmodifiableList());
    }

    private EventParticipant.StatusEnum toDomainEventParticipantStatus(CreateEventParticipant.StatusEnum createEventParticipantStatus) {
        switch (createEventParticipantStatus) {
            case EXPECTED:
                return EventParticipant.StatusEnum.EXPECTED;
            case HERE:
                return EventParticipant.StatusEnum.HERE;
            case MISSING:
                return EventParticipant.StatusEnum.MISSING;
            default:
                throw new BadRequestException("Unexpected EventParticipantType: " + createEventParticipantStatus.getValue());
        }
    }

     
}
