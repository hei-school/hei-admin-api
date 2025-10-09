package school.hei.haapi.service;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toUnmodifiableList;
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
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.LetterRepository;
import school.hei.haapi.repository.dao.EventParticipantDao;
import school.hei.haapi.service.utils.DateUtils.TimeRange;

@Service
@AllArgsConstructor
public class EventParticipantService {
  private final LetterRepository letterRepository;
  private final EventParticipantRepository eventParticipantRepository;
  private final EventRepository eventRepository;
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
          } else {
            // nothing
          }
        });
    eventParticipantRepository.saveAll(eventParticipants);
  }

  public EventParticipantStats getEventParticipantStats(
      String studentId, Optional<Instant> from, Optional<Instant> to) {
    if (from.isEmpty() && to.isEmpty()) {
      return generateParticipantStat(studentId, Optional.empty());
    }

    Instant fromInstant = from.orElse(Instant.now());
    Instant toInstant = to.orElse(Instant.now());

    if (fromInstant.isAfter(toInstant)) {
      throw new BadRequestException("Bad value for filters");
    }

    List<String> filteredEventsIds =
        eventRepository.findEventsBetweenInstant(fromInstant, toInstant).stream()
            .map(Event::getId)
            .collect(toUnmodifiableList());

    return generateParticipantStat(studentId, Optional.of(filteredEventsIds));
  }

  private EventParticipantStats generateParticipantStat(
      String studentId, Optional<List<String>> evenIds) {

    List<String> eventIdsList = evenIds.orElse(emptyList());
    var lateCount =
        eventParticipantDao
            .countEventParticipantByCriteria(studentId, LATE, eventIdsList)
            .intValue();
    var missedEventStats =
        evenIds.isEmpty()
            ? eventParticipantRepository.countMissedEventStatsByStudentId(studentId).toRest()
            : eventParticipantRepository
                .countMissedEventStatsByStudentIdAndEventIds(studentId, eventIdsList)
                .toRest();
    var presentCount =
        eventParticipantDao
            .countEventParticipantByCriteria(studentId, PRESENT, eventIdsList)
            .intValue();

    return new EventParticipantStats()
        .assistedEvents(presentCount)
        .lateEvents(lateCount)
        .missedEvents(missedEventStats)
        .totalEvents(lateCount + missedEventStats.getTotal() + presentCount);
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
    int late = eventParticipantRepository.countByEventIdAndStatus(eventId, LATE);
    int present = eventParticipantRepository.countByEventIdAndStatus(eventId, PRESENT);
    var missedStats =
        eventParticipantRepository.countMissedEventStatsByEventIds(List.of(eventId)).toRest();
    return new EventStats()
        .late(late)
        .missedStats(missedStats)
        .present(present)
        .total(eventParticipantRepository.countByEventId(eventId));
  }

  public EventStats getOverallEventStats() {
    var missedStats = eventParticipantRepository.countOverallMissedEventStats().toRest();
    int late = eventParticipantRepository.countByStatus(LATE);
    int present = eventParticipantRepository.countByStatus(PRESENT);

    return new EventStats()
        .late(late)
        .missedStats(missedStats)
        .present(present)
        .total(missedStats.getTotal() + present + late);
  }

  public EventStats getEventStats(List<String> eventIds) {
    int late = eventParticipantRepository.countByEventIdInAndStatus(eventIds, LATE);
    int present = eventParticipantRepository.countByEventIdInAndStatus(eventIds, PRESENT);
    var missedEventStats =
        eventParticipantRepository.countMissedEventStatsByEventIds(eventIds).toRest();
    return new EventStats()
        .late(late)
        .missedStats(missedEventStats)
        .present(present)
        .total(missedEventStats.getTotal() + present + late);
  }

  private boolean isParticipantAlreadyInEvent(String eventId, String groupId, String userId) {
    return eventParticipantRepository.existsByEventIdAndGroupIdAndParticipantId(
        eventId, groupId, userId);
  }
}
