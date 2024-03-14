package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.endpoint.rest.model.CreateEvent;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.EventService;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

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

  @GetMapping("/events")
  public List<Event> getEvents(
          @RequestParam(name = "page") PageFromOne page,
          @RequestParam(name = "page_size")BoundedPageSize pageSize,
          @RequestParam(name = "planner_name", required = false) String plannerName,
          @RequestParam(required = false) Instant from,
          @RequestParam(required = false) Instant to
          ) {
    return eventService.getEvents(plannerName, from, to, page, pageSize)
            .stream().map(mapper::toRest)
            .collect(Collectors.toUnmodifiableList());
  }
}
