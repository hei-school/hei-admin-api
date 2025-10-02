package school.hei.haapi.service;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.LATE;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.UNCHECKED;
import static school.hei.haapi.endpoint.rest.model.MissingStatus.JUSTIFIED;
import static school.hei.haapi.endpoint.rest.model.MissingStatus.UNJUSTIFIED;

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
import school.hei.haapi.endpoint.rest.model.MissingEventStats;
import school.hei.haapi.endpoint.rest.model.MissingStatus;
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
      MissingStatus missingStatus,
      TimeRange<Instant> eventBeginRange) {

    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "participant.ref"));

    return eventParticipantDao.findByCriteria(
        eventId, pageable, groupRef, name, ref, attendanceStatus, missingStatus, eventBeginRange);
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
      String participantId, Optional<Instant> from, Optional<Instant> to) {
    if (from.isEmpty() && to.isEmpty()) {
      return generateParticipantStat(participantId, Optional.empty());
    }

    Instant fromInstant = from.orElse(Instant.now());
    Instant toInstant = to.orElse(Instant.now());

    if (fromInstant.isBefore(toInstant)) {
      throw new BadRequestException("Bad value for filters");
    }

    List<String> filteredEventsIds =
        eventRepository.findEventsBetweenInstant(fromInstant, toInstant).stream()
            .map(Event::getId)
            .collect(toUnmodifiableList());

    return generateParticipantStat(participantId, Optional.of(filteredEventsIds));
  }

  private EventParticipantStats generateParticipantStat(
      String participantId, Optional<List<String>> evenIds) {
    Integer lateCount;
    Integer missingCount;
    Integer presentCount;

    if (evenIds.isPresent()) {
      List<String> eventIdsList = evenIds.get();
      lateCount =
          eventParticipantRepository.countAllByParticipantIdAndStatusAndEventIdIn(
              participantId, LATE, eventIdsList);
      missingCount =
          eventParticipantRepository.countAllByParticipantIdAndStatusAndEventIdIn(
              participantId, MISSING, eventIdsList);
      presentCount =
          eventParticipantRepository.countAllByParticipantIdAndStatusAndEventIdIn(
              participantId, PRESENT, eventIdsList);

      return new EventParticipantStats()
          .assistedEvents(presentCount)
          .lateEvents(lateCount)
          .missedEvents(missingCount)
          .totalEvents(lateCount + missingCount + presentCount);
    }

    lateCount = eventParticipantRepository.countAllByParticipantIdAndStatus(participantId, LATE);
    missingCount =
        eventParticipantRepository.countAllByParticipantIdAndStatus(participantId, MISSING);
    presentCount =
        eventParticipantRepository.countAllByParticipantIdAndStatus(participantId, PRESENT);

    return new EventParticipantStats()
        .assistedEvents(presentCount)
        .lateEvents(lateCount)
        .missedEvents(missingCount)
        .totalEvents(lateCount + missingCount + presentCount);
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

    return new EventStats()
        .late(late)
        .missing(countEventMissingStatsByEventId(eventId))
        .present(present)
        .total(eventParticipantRepository.countByEventId(eventId));
  }

  private MissingEventStats countEventMissingStatsByEventId(String eventId) {
    var justifiedMissingCount =
        eventParticipantDao.countMissingByMissingStatusAndEventId(JUSTIFIED, eventId).intValue();
    var unjustifiedMissingCount =
        eventParticipantDao.countMissingByMissingStatusAndEventId(UNJUSTIFIED, eventId).intValue();
    var totalMissingCount = eventParticipantRepository.countByEventIdAndStatus(eventId, MISSING);

    return new MissingEventStats()
        .total(totalMissingCount)
        .justified(justifiedMissingCount)
        .unjustified(unjustifiedMissingCount);
  }

  private MissingEventStats countOverallEventMissingStats() {
    int justifiedMissingCount =
        eventParticipantDao.countMissingByMissingStatus(JUSTIFIED).intValue();
    int unjustifiedMissingCount =
        eventParticipantDao.countMissingByMissingStatus(UNJUSTIFIED).intValue();
    int total = eventParticipantRepository.countByStatus(MISSING);

    return new MissingEventStats()
        .total(total)
        .justified(justifiedMissingCount)
        .unjustified(unjustifiedMissingCount);
  }

  public EventStats getOverallEventStats() {
    var missing = countOverallEventMissingStats();
    int late = eventParticipantRepository.countByStatus(LATE);
    int present = eventParticipantRepository.countByStatus(PRESENT);

    return new EventStats()
        .late(late)
        .missing(missing)
        .present(present)
        .total(missing.getTotal() + present + late);
  }

  private MissingEventStats countMissingEventStatsByEventIds(List<String> eventIds) {
    int justifiedMissingStats =
        eventParticipantDao.countMissingByMissingStatusAndEventIds(JUSTIFIED, eventIds).intValue();
    int unjustifiedMissingStats =
        eventParticipantDao
            .countMissingByMissingStatusAndEventIds(UNJUSTIFIED, eventIds)
            .intValue();
    int total = eventParticipantRepository.countByEventIdInAndStatus(eventIds, MISSING);

    return new MissingEventStats()
        .total(total)
        .justified(justifiedMissingStats)
        .unjustified(unjustifiedMissingStats);
  }

  public EventStats getEventStats(List<String> eventIds) {
    int late = eventParticipantRepository.countByEventIdInAndStatus(eventIds, LATE);
    int present = eventParticipantRepository.countByEventIdInAndStatus(eventIds, PRESENT);
    var missing = countMissingEventStatsByEventIds(eventIds);
    return new EventStats()
        .late(late)
        .missing(missing)
        .present(present)
        .total(missing.getTotal() + present + late);
  }

  private boolean isParticipantAlreadyInEvent(String eventId, String groupId, String userId) {
    return eventParticipantRepository.existsByEventIdAndGroupIdAndParticipantId(
        eventId, groupId, userId);
  }
}
