package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class SexEnumMapper {
  public Sex toRestSexEnum(User.Sex sex) {
    if (sex == null) {
      return null;
    }
    return switch (sex) {
      case F -> Sex.F;
      case M -> Sex.M;
      default -> throw new BadRequestException("Unexpected type " + sex);
    };
  }

  public User.Sex toDomainSexEnum(Sex sex) {
    if (sex == null) {
      return null;
    }
    return switch (sex) {
      case F -> User.Sex.F;
      case M -> User.Sex.M;
      default -> throw new BadRequestException("Unexpected type " + sex);
    };
  }
}
