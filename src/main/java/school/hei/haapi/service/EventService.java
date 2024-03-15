package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.dao.EventDao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;

@Service
@AllArgsConstructor
public class EventService {
  private final EventRepository eventRepository;
  private final EventDao eventDao;
  private final EventParticipantService eventParticipantService;
  private final UserService userService;

  public List<Event> createOrUpdateEvent(List<Event> eventToCrupdate) {
     List<Event> eventsCrupdated = eventRepository.saveAll(eventToCrupdate);
      for(Event event : eventsCrupdated){
          event.getGroups().forEach(group -> {
             List<User> users = userService.getByGroupId(group.getId());
             List<EventParticipant> eventParticipants = new ArrayList<>();
             users.forEach(user -> {
                 eventParticipants.add(
                         EventParticipant.builder()
                                 .event(event)
                                 .participant(user)
                                 .status(MISSING)
                                 .build()
                 );
             });
             eventParticipantService.crupdateEventParticipants(eventParticipants);
          });
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

  public List<Event> getEvents(String plannerName, Instant from, Instant to, PageFromOne page, BoundedPageSize pageSize){
      Pageable pageable =
              PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "begin"));
      return eventDao.findByCriteria(plannerName, from, to, pageable);
  }

}
