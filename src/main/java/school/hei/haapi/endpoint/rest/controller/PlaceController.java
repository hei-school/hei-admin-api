package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.mapper.PlaceMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.Place;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.PlaceService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class PlaceController {

  private final PlaceService placeService;
  private final PlaceMapper mapper;

  @GetMapping("/places")
  public List<Place> getAllPlaces (
      @RequestParam PageFromOne page,
      @RequestParam("page_size")BoundedPageSize pageSize
      ){
      return placeService.getAll(page, pageSize)
          .stream()
              .map(mapper :: toRest)
          .collect(Collectors.toUnmodifiableList());
  }

  @GetMapping("/places/{id}")
  public Place getPlaceById (@PathVariable String id){
    return mapper.toRest(placeService.getPlaceById(id));
  }

  @PutMapping("/places")
  public Place savePlace (
      @RequestBody Place toSave
  ){
    var saved = mapper.toDomain(toSave);
    return mapper.toRest(placeService.savePlace(saved));
  }
}
