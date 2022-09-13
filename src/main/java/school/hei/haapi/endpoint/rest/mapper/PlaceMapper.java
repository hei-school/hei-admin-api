package school.hei.haapi.endpoint.rest.mapper;

import lombok.Data;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Place;

@Component
public class PlaceMapper {
    public Place toRest(school.hei.haapi.model.Place place) {
        return new Place()
                .id(place.getId())
                .roomRef(place.getRoomRef())
                .name(place.getName());
    }

    public school.hei.haapi.model.Place toDomain(Place restPlace) {
        return school.hei.haapi.model.Place.builder()
                .id(restPlace.getId())
                .name(restPlace.getName())
                .roomRef(restPlace.getRoomRef())
                .build();
    }
}
