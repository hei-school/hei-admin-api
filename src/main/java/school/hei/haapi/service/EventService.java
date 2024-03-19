package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.EventType;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.dao.EventDao;

import java.time.Instant;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class EventService {
  private final EventRepository eventRepository;
  private final EventDao eventDao;
  private final EventParticipantService eventParticipantService;
  private final UserService userService;
  private final GroupService groupService;

  public List<Event> createOrUpdateEvent(List<Event> eventToCrupdate) {
    List<Event> eventsCrupdated = eventRepository.saveAll(eventToCrupdate);
    for (Event event : eventsCrupdated) {
      event
          .getGroups()
              .forEach(group -> eventParticipantService.crupdateEventParticipantsForAGroup(group, event));
    }
    return eventsCrupdated;
  }


  public Event findEventById(String eventId) {
    return eventRepository
        .findById(eventId)
        .orElseThrow(
            () -> {
              throw new NotFoundException("Event with id #" + eventId + " not found");
            });
  }

  public List<Event> getEvents(
          Instant from, Instant to, EventType eventType, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "begin"));
    return eventDao.findByCriteria(from, to, eventType, pageable);
  }
}
