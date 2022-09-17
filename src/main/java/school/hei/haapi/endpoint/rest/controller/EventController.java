package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.EventService;

@RestController
@AllArgsConstructor
public class EventController {

  private final EventService eventService;
  private final EventMapper eventMapper;

  @GetMapping(value = "/events/{id}")
  public Event getEventById(@PathVariable String id) {
    return eventMapper.toRest(eventService.getById(id));
  }

  @GetMapping(value = "/events")
  public List<Event> getEvents(@RequestParam("page") PageFromOne page, @RequestParam("page_size") BoundedPageSize pageSize) {
    return eventService.getAll(page,pageSize).stream()
        .map(eventMapper::toRest).collect(Collectors.toUnmodifiableList());
  }

  @PutMapping(value = "/events")
  public List<Event> createOrUpdateEvents(@RequestBody List<Event> toWrite) {
    var saved = toWrite.stream().map(eventMapper::toDomain).collect(Collectors.toUnmodifiableList());
    return eventService.saveAll(saved).stream().map(eventMapper::toRest).collect(Collectors.toUnmodifiableList());
  }

  @DeleteMapping(value = "/events/{id}")
  public String deleteEventById(@PathVariable String id) {
    eventService.deleteById(id);
    return "Resource successfully deleted";
  }
}
