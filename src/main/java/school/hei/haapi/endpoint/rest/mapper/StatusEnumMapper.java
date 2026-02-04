package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.endpoint.rest.model.EnableStatus.ALUMNI;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.DISABLED;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.SUSPENDED;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.model.User.Status;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class StatusEnumMapper {
  public EnableStatus toRestStatus(Status status) {
    if (status == null) {
      return null;
    }
    return switch (status) {
      case ENABLED -> ENABLED;
      case DISABLED -> DISABLED;
      case SUSPENDED -> SUSPENDED;
      case ALUMNI -> ALUMNI;
      default -> throw new BadRequestException("Unexpected type " + status);
    };
  }

  public Status toDomainStatus(EnableStatus status) {
    if (status == null) {
      return null;
    }
    return switch (status) {
      case ENABLED -> Status.ENABLED;
      case DISABLED -> Status.DISABLED;
      case SUSPENDED -> Status.SUSPENDED;
      case ALUMNI -> Status.ALUMNI;
      default -> throw new BadRequestException("Unexpected type " + status);
    };
  }
}
