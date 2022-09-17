package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.EventRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@AllArgsConstructor
public class EventService {

  private final EventRepository repository;

  public List<Event> getAll(PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(page.getValue()-1, pageSize.getValue());
    return repository.findAll(pageable).getContent();
  }

  public Event getById(String eventId) {
    return repository.getById(eventId);
  }

  @Transactional
  public List<Event> saveAll(List<Event> toCreate) {
    return repository.saveAll(toCreate);
  }

  public void deleteById(String eventId) {
    repository.deleteById(eventId);
  }
}
