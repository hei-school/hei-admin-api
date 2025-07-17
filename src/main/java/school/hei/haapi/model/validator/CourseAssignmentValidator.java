package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class CourseAssignmentValidator implements Consumer<CourseAssignment> {

  public void accept(List<CourseAssignment> courseAssignments) {
    courseAssignments.forEach(this);
  }

  @Override
  public void accept(CourseAssignment courseAssignment) {
    Set<String> violationMessages = new HashSet<>();
    if (courseAssignment.getCourse() == null) {
      violationMessages.add("Course is mandatory");
    }
    if (courseAssignment.getMainTeacher() == null) {
      violationMessages.add("Teacher is mandatory");
    }
    if (courseAssignment.getGroups() == null) {
      violationMessages.add("Groups are mandatory");
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages =
          violationMessages.stream().map(String::toString).collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
