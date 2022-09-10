package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Place;

@AllArgsConstructor
@Component
public class PlaceMapper {

  public Place toRest(school.hei.haapi.model.Place place){
    return new Place()
        .id(place.getId())
        .name(place.getName())
        .location(place.getLocation())
        .region(place.getRegion());
  }
  public school.hei.haapi.model.Place toDomain (Place rest){
    return school.hei.haapi.model.Place.builder()
        .id(rest.getId())
        .name(rest.getName())
        .location(rest.getLocation())
        .region(rest.getRegion())
        .build();
  }
}
