package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class StatusEnumMapper {
  public EnableStatus toRestStatus(User.Status status) {
    if (status == null) {
      return null;
    }
    return switch (status) {
      case ENABLED -> EnableStatus.ENABLED;
      case DISABLED -> EnableStatus.DISABLED;
      case SUSPENDED -> EnableStatus.SUSPENDED;
      default -> throw new BadRequestException("Unexpected type " + status);
    };
  }

  public User.Status toDomainStatus(EnableStatus status) {
    if (status == null) {
      return null;
    }
    return switch (status) {
      case ENABLED -> User.Status.ENABLED;
      case DISABLED -> User.Status.DISABLED;
      case SUSPENDED -> User.Status.SUSPENDED;
      default -> throw new BadRequestException("Unexpected type " + status);
    };
  }
}
