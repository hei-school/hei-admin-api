package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventParticipantMapper;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.EventParticipantService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@CrossOrigin
public class EventParticipantController {
    private EventParticipantService eventParticipantService;
    private EventParticipantMapper eventParticipantMapper;

    @GetMapping("events/{event_id}/eventParticipants/{event_participant_id}")
    public EventParticipant getEventParticipantById(
            @PathVariable String event_id,
            @PathVariable String event_participant_id
    ){
        return eventParticipantMapper.toRest(eventParticipantService.getById(event_id, event_participant_id));
    }
    @GetMapping("events/{event_id}/eventParticipants")
    public List<EventParticipant> getEventParticipants(
            @PathVariable String event_id,
            @RequestParam PageFromOne page,
            @RequestParam("page_size") BoundedPageSize pageSize
    ){
        return eventParticipantService.getAll(event_id, page, pageSize).stream()
                .map(eventParticipantMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PutMapping("/eventParticipants")
    public List<EventParticipant> createOrUpdateEventParticipants(
            @RequestBody List<EventParticipant> toWrite){
        return eventParticipantService
                .saveAll(toWrite)
                    .stream()
                    .map(eventParticipantMapper::toDomain)
                    .collect(Collectors.toUnmodifiableList())
                .stream()
                .map(eventParticipantMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }
}
