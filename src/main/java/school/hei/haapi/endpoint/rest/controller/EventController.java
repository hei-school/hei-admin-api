package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.endpoint.rest.mapper.EventParticipantMapper;
import school.hei.haapi.endpoint.rest.model.CreateEvent;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.EventParticipantService;
import school.hei.haapi.service.EventService;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
public class EventController {
  private final EventMapper mapper;
  private final EventParticipantMapper eventParticipantMapper;
  private final EventService eventService;
  private final EventParticipantService eventParticipantService;

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

  @GetMapping("/events/{event_id}/participants")
  public List<EventParticipant> getEventParticipants(
          @PathVariable(name = "event_id") String eventId,
          @RequestParam(name = "page", defaultValue = "1") PageFromOne page,
          @RequestParam(name = "page_size", defaultValue = "15") BoundedPageSize pageSize,
          @RequestParam(name = "group_id", required = false) String groupId
  ){
    return eventParticipantService.getEventParticipants(eventId, page, pageSize, groupId)
            .stream().map(eventParticipantMapper::toRest)
            .collect(Collectors.toUnmodifiableList());
  }

  @PutMapping("/events/{event_id}/participants")
  public List<EventParticipant> crupdateEventParticipantsToAnEvent(
          @PathVariable(name = "event_id") String eventId,
          @RequestBody List<EventParticipant> eventParticipantsToSave
  ){
    return eventParticipantService.crupdateEventParticipants(
      eventParticipantsToSave.stream().map(eventParticipant -> eventParticipantMapper.toDomain(eventParticipant, eventId)).collect(Collectors.toUnmodifiableList())
    ).stream().map(eventParticipantMapper::toRest).collect(Collectors.toUnmodifiableList());
  }

}
