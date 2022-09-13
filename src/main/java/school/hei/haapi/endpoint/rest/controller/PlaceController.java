package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.PlaceMapper;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.endpoint.rest.model.Place;
import school.hei.haapi.model.exception.NotImplementedException;
import school.hei.haapi.service.PlaceService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/places")
public class PlaceController {
    private final PlaceService placeService;
    private final PlaceMapper placeMapper;

    @GetMapping("")
    public List<Place> getPlaces() {
        return placeService.getAll().stream()
                .map(placeMapper::toRest)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Place getPlaceById(@PathVariable String id) {
        return placeMapper.toRest(placeService.getById(id));
    }

    @PutMapping("")
    public List<Place> createOrUpdate(@RequestBody List<Place> places) {
        return placeService.createOrUpdate(
                        places.stream().map(placeMapper::toDomain).collect(Collectors.toList())
                ).stream()
                .map(placeMapper::toRest)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/events")
    public List<Event> getEventFromOnePlace(@PathVariable String id){
        throw new NotImplementedException("Not implemented");
    }
}
