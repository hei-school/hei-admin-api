package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.User;
import school.hei.haapi.service.EventParticipantService;

@Component
@AllArgsConstructor
public class EventParticipantMapper {

  private final EventParticipantService eventParticipantService;
  private final GroupMapper groupMapper;

  public EventParticipant toDomain(
      school.hei.haapi.endpoint.rest.model.UpdateEventParticipant updateEventParticipant) {

    EventParticipant eventParticipant =
        eventParticipantService.findById(updateEventParticipant.getId());
    eventParticipant.setStatus(updateEventParticipant.getEventStatus());
    return eventParticipant;
  }

  public school.hei.haapi.endpoint.rest.model.EventParticipant toRest(EventParticipant domain) {
    User participant = domain.getParticipant();
    return new school.hei.haapi.endpoint.rest.model.EventParticipant()
        .email(participant.getEmail())
        .eventStatus(domain.getStatus())
        .id(domain.getId())
        .nic(participant.getNic())
        .ref(participant.getRef())
        .firstName(participant.getFirstName())
        .lastName(participant.getLastName())
        .group(groupMapper.toRestGroupIdentifier(domain.getGroup()));
  }
}
