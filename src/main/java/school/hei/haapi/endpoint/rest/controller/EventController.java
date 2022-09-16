package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.EventMapper;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.EventService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final EventMapper eventMapper;

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


}
