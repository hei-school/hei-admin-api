package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.exception.BadRequestException;

@AllArgsConstructor
@Component
public class CourseValidator implements Consumer<Course> {
  private Validator validator;

  public void accept(List<Course> courses) {
    courses.forEach(this::accept);
  }

  @Override
  public void accept(Course course) {
    Set<ConstraintViolation<Course>> violationMessages = validator.validate(course);
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages = violationMessages.stream()
          .map(ConstraintViolation::getMessage)
          .collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
