package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventParticipantMapper;
import school.hei.haapi.endpoint.rest.model.CreateEventParticipants;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.EventParticipantService;

@RestController
@AllArgsConstructor
public class EventParticipantController {

  private final EventParticipantService service;
  private final EventParticipantMapper mapper;

  @GetMapping(value = "/events/{event_id}/participants")
  public List<EventParticipant> getAll(@PathVariable String event_id,
                                       @RequestParam(value = "ref", required = false, defaultValue = "")
                                       String ref, @RequestParam("page")
                                       PageFromOne page,
                                       @RequestParam("page_size") BoundedPageSize pageSize,
                                       @RequestParam(value = "status", required = false, defaultValue = "EXPECTED")
                                       String status
  ) {
    return service.getAll(page, pageSize, event_id, ref, status).stream()
        .map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }

  @GetMapping(value = "/events/{event_id}/participants/{id}")
  public EventParticipant getById(@PathVariable("event_id") String eventId,
                                  @PathVariable("id") String id) {
    return mapper.toRest(service.getById(eventId, id));
  }

  @PostMapping(value = "/events/{event_id}/participants")
  public List<EventParticipant> createEventParticipants(
      @RequestBody List<CreateEventParticipants> toCreate, @PathVariable String event_id) {
    List<school.hei.haapi.model.EventParticipant> eventParticipantList =
        mapper.toSingleList(
            toCreate.stream().map((event) -> mapper.toDomain(event, event_id)).collect(
                Collectors.toUnmodifiableList()));

    return service.saveAll(eventParticipantList).stream()
        .map(mapper::toRest).collect(Collectors.toUnmodifiableList());
  }

  @PutMapping(value = "/events/{event_id}/participants")
  public List<EventParticipant> createOrUpdateEventParticipant(
      @PathVariable("event_id") String event_id,
      @RequestBody List<EventParticipant> eventParticipants) {
    return service.saveAll(
            eventParticipants.stream().map((event) -> mapper.toDomain(event, event_id))
                .collect(Collectors.toUnmodifiableList()))
        .stream().map(mapper::toRest).collect(
            Collectors.toUnmodifiableList());
  }

  @PostMapping(value = "/events/{event_id}/participants/attendance")
  public String checkAttendance(
      @RequestBody byte[] toCompare,
      @PathVariable("event_id") String event_id
  ) {
    return service.checkAttendance(event_id, toCompare);
  }
}
