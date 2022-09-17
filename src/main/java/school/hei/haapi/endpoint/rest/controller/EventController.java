package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.EventService;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableList;

@CrossOrigin
@RestController
@AllArgsConstructor
public class EventController {
    private EventService eventService;
    private EventMapper eventMapper;

    @GetMapping("/events/{id}")
    public Event getEventById(@PathVariable String id){
        return eventMapper.toRestEvent(eventService.getById(id));
    }

    @GetMapping("/responsibles/{id_responsible}/events")
    public List<Event> getEventsByResponsibleId(
            @PathVariable String id_responsible,
            @RequestParam PageFromOne page,
            @RequestParam("page_size") BoundedPageSize pageSize){
        return eventService.getEventsByResponsibleId(id_responsible,page,pageSize).stream()
                .map(eventMapper::toRestEvent)
                .collect(Collectors.toUnmodifiableList());
    }

    @GetMapping("/events")
    public List<Event> getEvents(@RequestParam PageFromOne page,
                                 @RequestParam("page_size") BoundedPageSize pageSize,
                                 @RequestParam(value = "name", required = false, defaultValue = "") String name){
        return eventService.getAll(page, pageSize,name).stream()
                .map(eventMapper::toRestEvent)
                .collect(Collectors.toUnmodifiableList());
    }

    @PutMapping("/events")
    public List<Event> createOrUpdateEvents(@RequestBody List<Event> toWrite){
        return eventService
                .saveAll(toWrite
                        .stream()
                        .map(eventMapper::toDomain)
                        .collect(toUnmodifiableList()))
                .stream()
                .map(eventMapper::toRestEvent)
                .collect(toUnmodifiableList());
    }
}
