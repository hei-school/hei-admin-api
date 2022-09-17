package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.service.EventService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/events")
public class EventController {
    private EventService eventService;
    private EventMapper eventMapper;

    @GetMapping(value = "")
    public List<Event> getEvents(@RequestParam(name = "place_id", required = false) String placeId) {
        return eventService.getAll(placeId).stream()
                .map(eventMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PutMapping(value = "")
    public List<Event> createOrUpdateEvents(@RequestBody List<Event> eventList) {
        return eventService.saveAll(
                        eventList.stream()
                                .map(eventMapper::toDomain)
                                .collect(Collectors.toUnmodifiableList())
                ).stream().map(eventMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @GetMapping(value = "/{id}")
    public Event getEventById(@PathVariable String id) {
        return eventMapper.toRest(eventService.getById(id));
    }
}
