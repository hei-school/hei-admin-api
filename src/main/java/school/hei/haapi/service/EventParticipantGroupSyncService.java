package school.hei.haapi.service;

import static java.time.Instant.now;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.UNCHECKED;
import static school.hei.haapi.model.User.Status.ENABLED;
import static school.hei.haapi.model.User.Status.SUSPENDED;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.dao.EventDao;

/**
 * Keeps {@link EventParticipant} rows (the roster an event's attendance is taken against) aligned
 * with a student's current group membership. Without this, a student moved between groups keeps
 * showing up in their old group's upcoming events and is missing from the new group's, since event
 * participants are otherwise only populated once, when an event is created or edited.
 */
@Service
@AllArgsConstructor
public class EventParticipantGroupSyncService {
  private final EventParticipantRepository eventParticipantRepository;
  private final EventDao eventDao;

  public void addStudentToFutureEventsOfGroup(User student, Group group) {
    if (!List.of(ENABLED, SUSPENDED).contains(student.getStatus())) {
      return;
    }
    var futureEvents =
        eventDao.findByCriteria(null, now(), null, null, group.getId(), null, null, null);
    var newParticipants =
        futureEvents.stream()
            .filter(
                event ->
                    !eventParticipantRepository.existsByEventIdAndParticipantId(
                        event.getId(), student.getId()))
            .map(
                event ->
                    EventParticipant.builder()
                        .participant(student)
                        .group(group)
                        .event(event)
                        .status(SUSPENDED.equals(student.getStatus()) ? MISSING : UNCHECKED)
                        .build())
            .toList();
    eventParticipantRepository.saveAll(newParticipants);
  }

  public void removeStudentFromFutureEventsOfGroup(User student, Group group) {
    var toRemove =
        eventParticipantRepository.findAllByParticipantIdAndGroupIdAndEventBeginDatetimeAfter(
            student.getId(), group.getId(), now());
    eventParticipantRepository.deleteAll(toRemove);
  }
}
