package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.model.Place;

@Component
public class PlaceMapper {

    public Place toRest(Place place) {
        Place restPlace = new Place();
        restPlace.setId(place.getId());
        restPlace.setName(place.getName());
        return restPlace;
    }

    public Place toDomain(Place place) {
        return Place.builder()
                .id(place.getId())
                .name(place.getName())
                .build();
    }

}