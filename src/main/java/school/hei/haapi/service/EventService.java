package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.EventRepository;

@Service
@AllArgsConstructor
public class EventService {
  private final EventRepository eventRepository;

  public List<Event> createOrUpdateEvent(List<Event> eventToCrupdate) {
    return eventRepository.saveAll(eventToCrupdate);
  }

  public Event findEventById(String eventId) {
    return eventRepository
        .findById(eventId)
        .orElseThrow(
            () -> {
              throw new NotFoundException("Event with id #" + eventId + " not found");
            });
  }
}
