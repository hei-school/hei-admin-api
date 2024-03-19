package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.EventParticipantRepository;

@Service
@AllArgsConstructor
public class EventParticipantService {

  private final EventParticipantRepository eventParticipantRepository;
  private final UserService userService;
  private final GroupService groupService;

  public List<EventParticipant> getEventParticipants(
      String eventId, PageFromOne page, BoundedPageSize pageSize, String groupRef) {

    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "id"));

    return Objects.isNull(groupRef)
        ? eventParticipantRepository.findAllByEventId(eventId, pageable)
        : eventParticipantRepository.findAllByEventIdAndGroupRef(eventId, groupRef, pageable);
  }

  public List<EventParticipant> crupdateEventParticipants(
      List<EventParticipant> eventParticipants) {
    return eventParticipantRepository.saveAll(eventParticipants);
  }

  public void crupdateEventParticipantsForAGroup(Group group, Event event) {
    List<User> users = userService.getByGroupId(group.getId());
    List<EventParticipant> eventParticipants = new ArrayList<>();
    Group actualGroup = groupService.getById(group.getId());
    users.forEach(
        user -> {
          eventParticipants.add(
              EventParticipant.builder()
                  .event(event)
                  .participant(user)
                  .status(MISSING)
                  .group(actualGroup)
                  .build());
        });
    crupdateEventParticipants(eventParticipants);
  }
}
