package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventParticipantMapper;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.service.EventParticipantService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@CrossOrigin
public class EventParticipantController {
    private EventParticipantService eventParticipantService;
    private EventParticipantMapper eventParticipantMapper;

    @GetMapping("/eventParticipants/{id}")
    public EventParticipant getEventParticipantById(@PathVariable String id){
        return eventParticipantMapper.toRest(eventParticipantService.getById(id));
    }

    @PutMapping("/eventParticipants")
    public List<EventParticipant> createOrUpdateEventParticipants(
            @RequestBody List<EventParticipant> toWrite){
        var saved = eventParticipantService.saveAll(toWrite.stream()
                .map(eventParticipantMapper::toDomain)
                .collect(Collectors.toUnmodifiableList()));
        return saved.stream()
                .map(eventParticipantMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }
}
