package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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

  public List<Event> getEvents(String plannerName, Instant from, Instant to, PageFromOne page, BoundedPageSize pageSize){
      Pageable pageable =
              PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "begin"));
      return eventDao.findByCriteria(plannerName, from, to, pageable);
  }

}
