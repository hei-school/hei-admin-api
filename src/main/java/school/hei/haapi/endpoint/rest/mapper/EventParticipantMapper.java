package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateEventParticipants;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.service.EventService;
import school.hei.haapi.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class EventParticipantMapper {
  private final UserService userService;
  private final EventService eventService;

  public EventParticipant toRest(school.hei.haapi.model.EventParticipant domain){
    EventParticipant eventParticipant = new EventParticipant()
        .id(domain.getId())
        .participantRef(domain.getUser().getRef())
        .eventId(domain.getEvent().getId())
        .status(EventParticipant.StatusEnum.fromValue(domain.getStatus().toString()));
    return eventParticipant;
  }
  public List<school.hei.haapi.model.EventParticipant> toDomain(CreateEventParticipants rest){
    List<school.hei.haapi.model.EventParticipant> participants = new ArrayList<>();
    List<User> users =  userService.getByGroup(new PageFromOne(1), new BoundedPageSize(100),
        User.Role.STUDENT, rest.getGroupId());
    Event event = eventService.getbyId(rest.getEventId());
    for (User user : users) {
      participants.add(
          school.hei.haapi.model.EventParticipant
              .builder()
              .id(user.getId())
              .user(user)
              .event(event)
              .status(school.hei.haapi.model.EventParticipant.StatusEnum.EXPECTED)
              .build()
      );
    }
    return participants;
  }
  public List<school.hei.haapi.model.EventParticipant> toSingleList(
      List<List<school.hei.haapi.model.EventParticipant>> toList) {
    List<school.hei.haapi.model.EventParticipant> participants = new ArrayList<>();
    for (List<school.hei.haapi.model.EventParticipant> eventParticipants : toList) {
      participants.addAll(eventParticipants);
    }
    return participants;
  }
}
