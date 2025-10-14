package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.EventParticipantStats;
import school.hei.haapi.endpoint.rest.model.EventStats;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.dao.EventParticipantDao;
import school.hei.haapi.service.utils.DateUtils.TimeRange;

@Service
@AllArgsConstructor
public class EventParticipantService {
  private final EventParticipantRepository eventParticipantRepository;
  private final UserService userService;
  private final GroupService groupService;
  private final EventParticipantDao eventParticipantDao;

  public List<EventParticipant> getEventParticipants(
      String eventId,
      PageFromOne page,
      BoundedPageSize pageSize,
      String groupRef,
      String name,
      String ref,
      AttendanceStatus attendanceStatus,
      TimeRange<Instant> eventBeginRange) {

    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "participant.ref"));

    return eventParticipantDao.findByCriteria(
        eventId, pageable, groupRef, name, ref, attendanceStatus, eventBeginRange);
  }

  public List<EventParticipant> updateEventParticipants(List<EventParticipant> eventParticipants) {
    return eventParticipantRepository.saveAll(eventParticipants);
  }

  public void createEventParticipantsForAGroup(Group group, Event event) {
    String groupId = group.getId();
    String eventId = event.getId();
    List<User> users = userService.getByGroupId(groupId, Pageable.unpaged());
    List<EventParticipant> eventParticipants = new ArrayList<>();
    Group actualGroup = groupService.findById(groupId);
    users.forEach(
        user -> {
          if (!isParticipantAlreadyInEvent(eventId, groupId, user.getId())) {
            EventParticipant newEventParticipant =
                EventParticipant.builder()
                    .participant(user)
                    .group(actualGroup)
                    .event(event)
                    .status(UNCHECKED)
                    .build();
            eventParticipants.add(newEventParticipant);
          }
        });
    eventParticipantRepository.saveAll(eventParticipants);
  }

  public EventParticipantStats getStudentEventStats(
      String studentId, Optional<Instant> from, Optional<Instant> to) {
    if (from.isEmpty() && to.isEmpty()) {
      return generateParticipantStat(studentId);
    }

    Instant fromInstant = from.orElse(Instant.now());
    Instant toInstant = to.orElse(Instant.now());

    if (fromInstant.isAfter(toInstant)) {
      throw new BadRequestException("Bad value for filters");
    }

    return generateParticipantStat(studentId, fromInstant, toInstant);
  }

  private EventParticipantStats generateParticipantStat(
      String studentId, Instant from, Instant to) {
    return eventParticipantRepository
        .countEventStatsByStudentIdAndEventBeginBetween(studentId, from, to)
        .toEventParticipantStats();
  }

  private EventParticipantStats generateParticipantStat(String studentId) {
    return eventParticipantRepository
        .countEventStatsByStudentId(studentId)
        .toEventParticipantStats();
  }

  public EventParticipant findById(String id) {
    return eventParticipantRepository
        .findById(id)
        .orElseThrow(
            () -> new NotFoundException("Event participant with id #" + id + "does not exist"));
  }

  public List<EventParticipant> findByEventId(String eventId, Pageable pageable) {
    return eventParticipantRepository
        .findAllByEventId(eventId, pageable)
        .orElseThrow(() -> new NotFoundException("Event with id #" + eventId + " does not exist"));
  }

  public List<EventParticipant> findByEventIdAndGroupRef(
      String eventId, String groupRef, Pageable pageable) {
    return eventParticipantRepository
        .findAllByEventIdAndGroupRef(eventId, groupRef, pageable)
        .orElseThrow(() -> new NotFoundException("Event with id #" + eventId + " does not exist"));
  }

  public EventStats getEventStats(String eventId) {
    return eventParticipantRepository.countEventStatsByEventId(eventId).toEventStats();
  }

  public EventStats getOverallEventStats() {
    return eventParticipantRepository.countOverallEventStats().toEventStats();
  }

  public EventStats getEventStats(Instant from, Instant to) {
    return eventParticipantRepository.countEventStatsByEventBeginBetween(from, to).toEventStats();
  }

  private boolean isParticipantAlreadyInEvent(String eventId, String groupId, String userId) {
    return eventParticipantRepository.existsByEventIdAndGroupIdAndParticipantId(
        eventId, groupId, userId);
  }
}
