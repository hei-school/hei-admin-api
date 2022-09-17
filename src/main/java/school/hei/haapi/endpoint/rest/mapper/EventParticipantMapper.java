package school.hei.haapi.endpoint.rest.mapper;

import java.util.ArrayList;
import java.util.List;
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

@Component
@AllArgsConstructor
public class EventParticipantMapper {
  private final UserService userService;
  private final EventService eventService;

  public EventParticipant toRest(school.hei.haapi.model.EventParticipant domain) {
    return new EventParticipant()
        .id(domain.getId())
        .studentId(domain.getUser().getId())
        .eventId(domain.getEvent().getId())
        .status(EventParticipant.StatusEnum.valueOf(domain.getStatus().name()));
  }

  public school.hei.haapi.model.EventParticipant toDomain(EventParticipant participant,String event_id) {
    User user = userService.getById(participant.getStudentId());
    return school.hei.haapi.model.EventParticipant
        .builder()
        .id(participant.getId())
        .user(user)
        .status(school.hei.haapi.model.EventParticipant.StatusEnum.valueOf(participant.getStatus().name()))
        .event(eventService.getById(event_id))
        .build();
  }

  public List<school.hei.haapi.model.EventParticipant> toDomain(CreateEventParticipants toCreate, String event_id) {
    final int MAX_PAGE = 500;
    List<school.hei.haapi.model.EventParticipant> participants = new ArrayList<>();
    List<User> students =
        userService.getByGroup(new PageFromOne(1), new BoundedPageSize(500),
            User.Role.STUDENT, toCreate.getGroupId());

    Event event = eventService.getById(event_id);
    for (User student : students) {
      participants.add(
          school.hei.haapi.model.EventParticipant
              .builder()
              .user(student)
              .event(event)
              .status(school.hei.haapi.model.EventParticipant.StatusEnum.EXPECTED)
              .build()
      );
    }
    return participants;
  }

  public List<school.hei.haapi.model.EventParticipant> toSingleList(
      List<List<school.hei.haapi.model.EventParticipant>> toConvert) {
    List<school.hei.haapi.model.EventParticipant> participants = new ArrayList<>();
    for (List<school.hei.haapi.model.EventParticipant> eventParticipants : toConvert) {
      participants.addAll(eventParticipants);
    }
    return participants;
  }
}
