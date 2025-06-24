package school.hei.haapi.model.validator;

import java.util.HashSet;
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

  public void accept(List<AwardedCourse> awardedCourses) {
    awardedCourses.forEach(this);
  }

  @Override
  public void accept(AwardedCourse awardedCourse) {
    Set<String> violationMessages = new HashSet<>();
    if (awardedCourse.getCourse() == null) {
      violationMessages.add("Course is mandatory");
    }
    if (awardedCourse.getMainTeacher() == null) {
      violationMessages.add("Teacher is mandatory");
    }
    if (awardedCourse.getGroup() == null) {
      violationMessages.add("Group is mandatory");
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages =
          violationMessages.stream().map(String::toString).collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
