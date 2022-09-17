package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventParticipantMapper;
import school.hei.haapi.endpoint.rest.model.CreateEventParticipant;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.service.EventParticipantService;

import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.stream.Collectors;

import school.hei.haapi.endpoint.rest.model.EventParticipant.StatusEnum;

@RestController
@AllArgsConstructor
public class EventParticipantController {
    private EventParticipantService eventParticipantService;
    private EventParticipantMapper eventParticipantMapper;

    @GetMapping(value = "/events/{event_id}/event_participants/{event_participant_id}")
    public EventParticipant getEventEventParticipantById(
            @PathVariable String event_id,
            @PathVariable String event_participant_id
    ) {
        return eventParticipantMapper.toRest(eventParticipantService.getByEventIdAndId(event_id, event_participant_id));
    }

    @GetMapping(value = "/events/{event_id}/event_participants")
    public List<EventParticipant> getEventEventParticipants(
            @PathVariable String event_id,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "page_size", required = false) Integer page_size,
            @RequestParam(name = "status", required = false) StatusEnum status
    ) {
        return eventParticipantService.getAllByEventId(page, page_size, event_id, status)
                .stream().map(eventParticipantMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PostMapping(value = "/events/{event_id}/event_participants")
    public List<EventParticipant> createEventEventParticipants(
            @PathVariable String event_id,
            @RequestBody List<CreateEventParticipant> createEventParticipantList
    ) {
        List<EventParticipant> restEventParticipantList = createEventParticipantList.stream().map(
                        (createEventParticipant) -> eventParticipantMapper.toRestCreateEventParticipant(event_id, createEventParticipant))
                .collect(Collectors.toUnmodifiableList());
        return eventParticipantService
                .createAll(
                        restEventParticipantList.stream().map(eventParticipantMapper::toDomain)
                                .collect(Collectors.toUnmodifiableList())
                ).stream().map(eventParticipantMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PutMapping(value = "/events/{event_id}/presence")
    public EventParticipant presenceWithImage(
            @PathVariable String event_id,
            @RequestBody byte[] image
    ) throws NoSuchFileException {
        return eventParticipantMapper.toRest(eventParticipantService.updateStatusWithImage(event_id, image));
    }

    @PutMapping(value = "/events/{event_id}/event_participants")
    public List<EventParticipant> updateEventEventParticipants(
            @PathVariable String event_id,
            @RequestBody List<EventParticipant> eventParticipantList
    ) {
        return eventParticipantService.updateAll(
                        event_id,
                        eventParticipantList.stream().map(eventParticipantMapper::toDomain)
                                .collect(Collectors.toUnmodifiableList())
                ).stream().map(eventParticipantMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @GetMapping(value = "/event_participants")
    public List<EventParticipant> getEventParticipants(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "page_size", required = false) Integer page_size,
            @RequestParam(name = "status", required = false) StatusEnum status
    ) {
        return eventParticipantService.getAll(page, page_size, status)
                .stream().map(eventParticipantMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }
}
