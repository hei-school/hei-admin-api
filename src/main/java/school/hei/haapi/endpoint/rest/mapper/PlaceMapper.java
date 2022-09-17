package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Place;

@Component
public class PlaceMapper {

  public Place toRest(school.hei.haapi.model.Place domain) {
    return new Place()
        .id(domain.getId())
        .location(domain.getLocation())
        .city(domain.getCity());
  }

  public school.hei.haapi.model.Place toDomain(Place restPlace) {
    return school.hei.haapi.model.Place.builder()
        .location(restPlace.getLocation())
        .city(restPlace.getCity())
        .build();
  }
}
