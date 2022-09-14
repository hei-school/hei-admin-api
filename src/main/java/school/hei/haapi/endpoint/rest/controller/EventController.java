package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.model.Event;
import school.hei.haapi.service.EventService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@CrossOrigin
public class EventController {
    private EventService eventService;
    private EventMapper eventMapper;

    @GetMapping("/events/{id}")
    public Event getEventById(@PathVariable String id){
        return eventMapper.toRest(eventService.getById(id));
    }

    @GetMapping("/events")
    public List<Event> getEvents(){
        return eventService.getAll().stream()
                .map(eventMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PutMapping("/events")
    public List<Event> createOrUpdateEvents(@RequestBody List<Event> toWrite){
        return eventService
                .saveAll(toWrite)
                    .stream()
                    .map(eventMapper::toDomain)
                    .collect(Collectors.toUnmodifiableList())
                .stream()
                .map(eventMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }
}
