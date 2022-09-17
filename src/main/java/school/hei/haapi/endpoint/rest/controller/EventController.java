package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.EventService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class EventController {
  private final EventService service;
  private final EventMapper mapper;

  @GetMapping("/events/{id}")
  public Event getEventById(@RequestParam String id){
    return mapper.toRest(service.getbyId(id));
  }

  @GetMapping("/events")
  public List<Event> getAllEvents(
      @RequestParam PageFromOne page, @RequestParam("page_size") BoundedPageSize pageSize
      ){
    return service.getAll(page, pageSize)
        .stream()
        .map(mapper :: toRest)
        .collect(Collectors.toUnmodifiableList());
  }

  @PutMapping("/events")
  public List<Event> saveAllEvents (
      @RequestBody List<Event> eventsToSave
  ){
    return service.saveAll(eventsToSave
        .stream().map(mapper :: toDomain)
            .collect(Collectors.toUnmodifiableList()))
        .stream().map(mapper :: toRest)
        .collect(Collectors.toUnmodifiableList());
  }
}
