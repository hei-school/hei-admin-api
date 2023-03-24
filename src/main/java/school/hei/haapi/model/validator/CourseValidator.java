package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.CourseRepository;

@Component
@AllArgsConstructor
public class CourseValidator implements Consumer<Course> {
  private final CourseRepository repository;

  public void accept(List<Course> courses) {
    courses.forEach(this::accept);
  }

  @Override
  public void accept(Course course) {
    Set<String> violationMessages = new HashSet<>();
    if (course.getCode() == null) {
      violationMessages.add("Code is mandatory");
    }
    if (course.getId() == null && repository.existsByCode(course.getCode())) {
      violationMessages.add("Code must be unique");
    }
    if (course.getCredits() < 0) {
      violationMessages.add("Credits must be superior than 0");
    }
    if (course.getName() == null) {
      violationMessages.add("Name is mandatory");
    }
    if (course.getTotalHours() < 0) {
      violationMessages.add("Total hours must be superior than 0");
    }
    if (course.getMainTeacher() == null) {
      violationMessages.add("A course must have a main teacher");
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages = violationMessages.stream()
          .map(String::toString)
          .collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}

