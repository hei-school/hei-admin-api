package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.endpoint.rest.mapper.EventParticipantMapper;
import school.hei.haapi.endpoint.rest.model.CreateEventParticipant;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.EventParticipantService;
import school.hei.haapi.service.EventService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final EventMapper eventMapper;

    private final EventParticipantService eventParticipantService;
    private final EventParticipantMapper eventParticipantMapper;

    @GetMapping("")
    public List<Event> getAll(
            @RequestParam PageFromOne page, @RequestParam("page_size") BoundedPageSize pageSize
            , @RequestParam(required = false) String placeId) {
        return eventService.getAllOrByPlaceId(page, pageSize, placeId)
                .stream().map(eventMapper::toRest).collect(Collectors.toList());
    }

    @PutMapping("")
    public List<Event> createEvents(@RequestBody List<Event> events) {
        return eventService.createOrUpdate(events.stream()
                        .map(eventMapper::toDomain).collect(Collectors.toList())).stream()
                .map(eventMapper::toRest).collect(Collectors.toList());
    }

    @GetMapping("/{event_id}/participants")
    public List<EventParticipant> getEventsParticipants(
            @RequestParam(required = false, defaultValue = "5") PageFromOne page
            , @RequestParam(value = "page_size", required = false, defaultValue = "10") BoundedPageSize pageSize, @PathVariable(name = "event_id") String eventId) {
        Pageable pageable = PageRequest.of(page.getValue(), pageSize.getValue());
        return eventParticipantService.getsByEventIdOrAndType(pageable, eventId, null)
                .stream()
                .map(eventParticipantMapper::toRest)
                .collect(Collectors.toList());
    }

    @PutMapping("/{event_id}/participants")
    public List<EventParticipant> createEventParticipant(@PathVariable(name = "event_id") String eventId, @RequestBody List<CreateEventParticipant> participants) {
        return eventParticipantService.saveAll(
                        participants.stream()
                                .map(createEventParticipant -> eventParticipantMapper
                                        .toDomain(eventId, createEventParticipant)
                                )
                                .collect(Collectors.toList())
                ).stream()
                .map(eventParticipantMapper::toRest).collect(Collectors.toList());
    }

}
