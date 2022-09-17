package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.PlaceMapper;
import school.hei.haapi.endpoint.rest.model.Place;
import school.hei.haapi.service.PlaceService;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/places")
public class PlaceController {
    private PlaceService placeService;
    private PlaceMapper placeMapper;

    @GetMapping(value = "/{id}")
    public Place getPlaceById(@PathVariable String id) {
        return placeMapper.toRest(placeService.getById(id));
    }

    @GetMapping(value = "")
    public List<Place> getPlaces() {
        return placeService.getAll()
                .stream().map(placeMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PutMapping(value = "")
    public List<Place> createOrUpdatePlaces(@RequestBody List<Place> toWrite) {
        var saved = placeService.saveAll(toWrite.stream()
                .map(placeMapper::toDomain)
                .collect(toUnmodifiableList()));
        return saved.stream()
                .map(placeMapper::toRest)
                .collect(toUnmodifiableList());
    }
}
