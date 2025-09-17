package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.model.Event.PlaceName.ANDRAHARO;
import static school.hei.haapi.model.Event.PlaceName.IVANDRY;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.PlaceEnum;
import school.hei.haapi.model.Event;

@Component
public class PlaceMapper {
  public Event.PlaceName toDomain(PlaceEnum rest) {
    return switch (rest) {
      case IVANDRY -> IVANDRY;
      case ANDRAHARO -> ANDRAHARO;
      case null -> null;
    };
  }

  public PlaceEnum toRest(Event.PlaceName domain) {
    return switch (domain) {
      case IVANDRY -> PlaceEnum.IVANDRY;
      case ANDRAHARO -> PlaceEnum.ANDRAHARO;
      case null -> null;
    };
  }
}
