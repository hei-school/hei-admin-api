package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.model.Place;

@Component
public class PlaceMapper {
    public school.hei.haapi.endpoint.rest.model.Place toRest(Place place) {
        var restPlace = new school.hei.haapi.endpoint.rest.model.Place();
        restPlace.setId(place.getId());
        restPlace.setName(place.getName());
        return restPlace;
    }

    public Place toDomain(school.hei.haapi.endpoint.rest.model.Place place) {
        return Place.builder()
                .id(place.getId())
                .name(place.getName())
                .build();
    }
}
