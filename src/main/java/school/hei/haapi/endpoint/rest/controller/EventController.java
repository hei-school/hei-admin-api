package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.model.Event;
import school.hei.haapi.service.EventService;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventMapper eventMapper;

    @GetMapping(value = "/events/{id}")
    public Event getEventById(@PathVariable String id) {
        return eventMapper.toRest(eventService.getById(id));
    }

    @GetMapping(value = "/events")
    public List<Event> getEvents() {
        return eventService.getAll().stream()
                .map(eventMapper::toRest)
                .collect(toUnmodifiableList());
    }

    @PutMapping(value = "/events")
    public List<Event> createOrUpdateGroups(@RequestBody List<Event> toWrite) {
        var saved = eventService.saveAll(toWrite.stream()
                .map(eventMapper::toDomain)
                .collect(toUnmodifiableList()));
        return saved.stream()
                .map(eventMapper::toRest)
                .collect(toUnmodifiableList());
    }
}
