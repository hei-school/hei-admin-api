package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.PlaceMapper;
import school.hei.haapi.endpoint.rest.model.Place;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.PlaceService;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableList;

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
    public List<Place> getPlaces(@RequestParam PageFromOne page, @RequestParam("page_size") BoundedPageSize pageSize,
                                 @RequestParam(value = "name", required = false, defaultValue = "") String name){
        return placeService.getAll(page, pageSize,name).stream()
                .map(placeMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PutMapping("/places")
    public List<Place> createOrUpdatePlace(@RequestBody List<Place> toWrite){
        var saved = placeService.saveAll(toWrite.stream()
                .map(placeMapper::toDomain)
                .collect(toUnmodifiableList()));
        return saved.stream()
                .map(placeMapper::toRest)
                .collect(toUnmodifiableList());
    }
}
