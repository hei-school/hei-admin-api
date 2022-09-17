package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.PlaceMapper;
import school.hei.haapi.endpoint.rest.model.Place;
import school.hei.haapi.service.PlaceService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class PlaceController {

  private final PlaceService placeService;
  private final PlaceMapper placeMapper;

  @GetMapping(value = "/places")
  public List<Place> getPlaces() {
    return placeService.getAll().stream()
            .map(placeMapper::toRest)
            .collect(Collectors.toUnmodifiableList());
  }

  @PutMapping(value = "/places")
  public Place createOrUpdatePlace(@RequestBody Place place){
    return placeMapper.toRest(placeService.save(placeMapper.toDomain(place)));
  }
}
