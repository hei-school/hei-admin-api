package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.model.Place;
import school.hei.haapi.model.User;
import school.hei.haapi.service.PlaceService;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class EventMapper {

  private final PlaceService placeService;
  private final UserService userService;

  public Event toRest (school.hei.haapi.model.Event domain){
    Event event = new Event();
      event.setId(domain.getId());
      event.setEventName(domain.getEventName());
      event.setEventType(domain.getEventType());
      event.setStartDate(domain.getStartDate());
      event.setEndDate(domain.getEndDate());
      event.setPlace(domain.getPlace().getName());
    String responsible = domain.getResponsible().getFirstName();
      event.setResponsible(responsible);
      return event;
  }

  public school.hei.haapi.model.Event toDomain (Event rest){
    school.hei.haapi.model.Event event = new school.hei.haapi.model.Event();
      event.setId(rest.getId());
      event.setEventName(rest.getEventName());
      event.setEventType(rest.getEventType());
      event.setStartDate(rest.getStartDate());
    Place place = placeService.getPlaceByName(rest.getPlace());
      event.setPlace(place);
    User user =  userService.getByFirstName(rest.getResponsible());
      event.setResponsible(user);
    return event;
  }
}
