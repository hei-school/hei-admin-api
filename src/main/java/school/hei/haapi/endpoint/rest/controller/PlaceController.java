package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.PlaceMapper;
import school.hei.haapi.model.Place;
import school.hei.haapi.service.PlaceService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@CrossOrigin
public class PlaceController {
    private PlaceService placeService;

    private PlaceMapper placeMapper;

    @GetMapping("/places/{id}")
    public Place getPlaceById(@PathVariable String id){
        return placeMapper.toRest(placeService.getById(id));
    }

    @GetMapping("/places")
    public List<Place> getPlaces(){
        return placeService.getAll().stream()
                .map(placeMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PutMapping("/places")
    public Place createOrUpdatePlace(@RequestBody Place toWrite){
        var saved = placeService.save(placeMapper.toDomain(toWrite));
        return placeMapper.toRest(saved);
    }
}
