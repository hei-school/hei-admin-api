package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.model.Event.RoomName.ALGEBRE;
import static school.hei.haapi.model.Event.RoomName.B;
import static school.hei.haapi.model.Event.RoomName.NP;
import static school.hei.haapi.model.Event.RoomName.PI;
import static school.hei.haapi.model.Event.RoomName.SIGMA;
import static school.hei.haapi.model.Event.RoomName.UNKNOWN;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.RoomEnum;
import school.hei.haapi.model.Event;

@Component
public class RoomMapper {
  public Event.RoomName toDomain(RoomEnum rest) {
    return switch (rest) {
      case B -> B;
      case NP -> NP;
      case PI -> PI;
      case SIGMA -> SIGMA;
      case ALGEBRE -> ALGEBRE;
      case UNKNOWN -> UNKNOWN;
      case null -> UNKNOWN;
    };
  }

  public RoomEnum toRest(Event.RoomName domain) {
    return switch (domain) {
      case B -> RoomEnum.B;
      case NP -> RoomEnum.NP;
      case PI -> RoomEnum.PI;
      case SIGMA -> RoomEnum.SIGMA;
      case ALGEBRE -> RoomEnum.ALGEBRE;
      case UNKNOWN -> RoomEnum.UNKNOWN;
      case null -> RoomEnum.UNKNOWN;
    };
  }
}
