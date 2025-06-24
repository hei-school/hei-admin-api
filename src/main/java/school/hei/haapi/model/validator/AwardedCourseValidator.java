package school.hei.haapi.model.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class AwardedCourseValidator implements Consumer<AwardedCourse> {
  private final Validator validator;

  public void accept(List<AwardedCourse> awardedCourses) {
    awardedCourses.forEach(this);
  }

  @Override
  public void accept(AwardedCourse awardedCourse) {
    Set<ConstraintViolation<AwardedCourse>> violations = validator.validate(awardedCourse);
    if (awardedCourse.getCourse() == null) {
      throw new BadRequestException("Course is mandatory");
    }
    if (awardedCourse.getMainTeacher() == null) {
      throw new BadRequestException("Teacher is mandatory");
    }
    if (awardedCourse.getGroup() == null) {
      throw new BadRequestException("Group is mandatory");
    }
    if (!violations.isEmpty()) {
      String constraintMessages =
          violations.stream()
              .map(ConstraintViolation::getMessage)
              .collect(Collectors.joining(". "));
      throw new BadRequestException(constraintMessages);
    }
  }
}
