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
  private UserService userService;
  private PlaceService placeService;

  public Event toRest(school.hei.haapi.model.Event domain) {
    return new Event()
        .id(domain.getId())
        .responsible(domain.getResponsible().getId())
        .type(domain.getType())
        .place(domain.getPlace().getId())
        .startDatetime(domain.getStartDatetime())
        .endDatetime(domain.getEndDatetime());
  }

  public school.hei.haapi.model.Event toDomain(Event restEvent) {
    User responsible = userService.getById(restEvent.getResponsible());
    Place place = placeService.getById(restEvent.getPlace());
    return school.hei.haapi.model.Event.builder()
        .id(restEvent.getId())
        .responsible(responsible)
        .type(restEvent.getType())
        .place(place)
        .startDatetime(restEvent.getStartDatetime())
        .endDatetime(restEvent.getEndDatetime())
        .build();
  }
}
