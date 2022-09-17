package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.Place;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.PlaceService;
import school.hei.haapi.service.UserService;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class EventMapper {

    private final PlaceService placeService;
    private final UserService userService;

    public school.hei.haapi.endpoint.rest.model.Event toRest(Event event) {
        var restEvent = new school.hei.haapi.endpoint.rest.model.Event();
        restEvent.setId(event.getId());
        restEvent.setName(event.getName());
        restEvent.setRef(event.getRef());
        restEvent.setStartingHours(event.getStartingHours());
        restEvent.setEndingHours(event.getEndingHours());
        restEvent.setUserManagerId(event.getUserManager().getId());
        restEvent.setPlaceId(event.getPlace().getId());
        return restEvent;
    }

    public Event toDomain(school.hei.haapi.endpoint.rest.model.Event restEvent) {
        Place place = placeService.getById(restEvent.getPlaceId());
        User user = userService.getById(restEvent.getUserManagerId());
        if (place == null) {
            throw new NotFoundException("Place id=" + restEvent.getPlaceId() + " not found");
        }
        if (user == null) {
            throw new NotFoundException("User id=" + restEvent.getUserManagerId() + " not found");
        }
        return Event.builder()
                .id(restEvent.getId())
                .name(restEvent.getName())
                .ref(restEvent.getRef())
                .startingHours(restEvent.getStartingHours())
                .endingHours(restEvent.getEndingHours())
                .userManager(user)
                .place(place)
                .build();
    }
}
