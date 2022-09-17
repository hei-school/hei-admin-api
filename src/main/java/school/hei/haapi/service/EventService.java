package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.EventRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class EventService {

  private final EventRepository repository;

  public List<Event> getAll (PageFromOne page, BoundedPageSize pageSize){
    Pageable pageable = PageRequest.of(page.getValue() -1, pageSize.getValue() -1, Sort.by(Sort.Direction.ASC,
        "eventName"));
    return repository.findAll(pageable).toList();
  }

  public Event getbyId (String id){
    Event event = repository.getById(id);
    return event;
  }

  @Transactional
  public List<Event> saveAll (List<Event> events){
    List<Event> saved = repository.saveAll(events);
    return saved;
  }

}
