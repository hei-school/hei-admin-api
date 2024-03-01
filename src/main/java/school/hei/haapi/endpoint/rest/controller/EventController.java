package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.endpoint.rest.model.CreateEvent;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.service.EventService;

@AllArgsConstructor
@RestController
public class EventController {
  private final EventMapper mapper;
  private final EventService eventService;

  @PutMapping("/events")
  public List<Event> crupdateEvents(@RequestBody List<CreateEvent> eventsToSave) {
    return eventService
        .createOrUpdateEvent(eventsToSave.stream().map(mapper::toDomain).toList())
        .stream()
        .map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }

  @GetMapping("/events/{event_id}")
  public Event getEventById(@PathVariable(name = "event_id") String eventId) {
    return mapper.toRest(eventService.findEventById(eventId));
  }
}
