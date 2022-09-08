package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.model.Place;
import school.hei.haapi.repository.PlaceRepository;

import java.util.List;

@RestController
@AllArgsConstructor
public class PlaceController {

    private final PlaceRepository repository;

    @GetMapping("/place/{placeId}")
    public Place getPlacesByid(@PathVariable String placeId){
        return repository.getById(placeId);
    }
    @GetMapping("/place/")
    public List<Place> getAllPlaces(){
        return repository.findAll();
    }
}
