package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class EventParticipantController {
  private final EventParticipantService service;
  private final EventParticipantMapper mapper;

  @GetMapping("/events/{event_id}/participants")
  public List<EventParticipant> getAll(
      @PathVariable String event_id,
      @RequestParam(value = "ref", required = false, defaultValue = "") String ref,
      @RequestParam("page") PageFromOne page,
      @RequestParam("page_size") BoundedPageSize pageSize,
      @RequestParam(value = "status", required = false, defaultValue = "EXPECTED") String status
  ){
    return service.getAll(page,pageSize,event_id,ref,status)
        .stream().map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }

  @GetMapping("/events/{event_id}/participants/{id}")
  public EventParticipant getById(
      @PathVariable("event_id") String eventId,
      @PathVariable("id") String id
  ){
    return mapper.toRest(service.getByIdAndEvent(id,eventId));
  }

  @PostMapping(name = "/events/{event_id}/participants/perfomAttendance",
        consumes = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE}
  )
  public String performAttendance(
      @RequestBody byte[] image,
      @RequestParam String ref,
      @PathVariable String event_id
  ){
    return service.performAttendance(image,ref,event_id);
  }

  @PutMapping("/events/{event_id}/participants")
  public List<EventParticipant> save(
      @PathVariable("event_id") String eventId,
      @RequestBody List<CreateEventParticipants> toCreate
  ){
    List<school.hei.haapi.model.EventParticipant> eventParticipantList =
        mapper.toSingleList(toCreate.stream().map(mapper::toDomain).collect(Collectors.toUnmodifiableList()));
    return service.saveAll(eventParticipantList)
        .stream().map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }
}
