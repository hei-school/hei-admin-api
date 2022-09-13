package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Place;

@Component
@AllArgsConstructor
public class PlaceMapper {

    public Place toRest(Place place){
        var restPlace = new Place();
        restPlace.setId(place.getId());
        restPlace.setName(place.getName());
        return restPlace;
    }

    public Place toDomain(Place restPlace){
        return Place.builder()
                .id(restPlace.getId())
                .name(restPlace.getName())
                .build();
    }
}
