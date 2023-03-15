package school.hei.haapi.model.validator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;

import static school.hei.haapi.service.PromotionService.instantToLocalDate;

@Component
@AllArgsConstructor
public class UserValidator implements Consumer<User> {
  private final Validator validator;

  public void accept(List<User> users) {
    users.forEach(this::accept);
  }

  private boolean isValidEntranceDatetime(Instant entranceDatetime) {
    int year = entranceDatetime.atZone(ZoneOffset.UTC).getYear();
    LocalDate start = LocalDate.of(year, Month.JULY, 1);
    LocalDate end = LocalDate.of(year + 1, Month.AUGUST, 31);
    LocalDate toCheck = instantToLocalDate(entranceDatetime);
    return (toCheck.isEqual(start) || toCheck.isAfter(start) && toCheck.isBefore(end));
  }
  @Override
  public void accept(User user) {
    Set<ConstraintViolation<User>> violations = validator.validate(user);
    if (user.getEntranceDatetime() == null) {
      throw new BadRequestException("Entrance datetime is mandatory");
    }
    if (!isValidEntranceDatetime(user.getEntranceDatetime())) {
      throw new BadRequestException("entrance datetime is not valid !");
    }
    if (!violations.isEmpty()) {
      String constraintMessages = violations
              .stream()
              .map(ConstraintViolation::getMessage)
              .collect(Collectors.joining(". "));
      throw new BadRequestException(constraintMessages);
    }
  }
}
