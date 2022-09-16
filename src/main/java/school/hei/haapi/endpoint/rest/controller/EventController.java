package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.EventService;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableList;

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
    public List<Event> getEvents(
            @RequestParam PageFromOne page,
            @RequestParam("page_size") BoundedPageSize pageSize,
            @RequestParam(required = false, defaultValue = "") String name
    ){
        return eventService.getByName(name,page,pageSize).stream()
                .map(eventMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @GetMapping("/events/myevents")
    public List<Event> getMyEvents(
            @AuthenticationPrincipal Principal principal
            ){
        String supervisorId = principal.getUserId();
        return eventService.getAllBySupervisorId(supervisorId).stream()
                .map(eventMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PutMapping("/events")
    public List<Event> createOrUpdateEvents(@RequestBody List<Event> toWrite){
        var saved = eventService.saveAll(toWrite.stream()
                .map(eventMapper::toDomain)
                .collect(toUnmodifiableList()));
        return saved.stream()
                .map(eventMapper::toRest)
                .collect(toUnmodifiableList());
    }
}
