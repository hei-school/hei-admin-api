package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class CourseValidator implements Consumer<CrupdateCourse> {

  @Override
  public void accept(CrupdateCourse course) {
    Set<String> violationMessages = new HashSet<>();
    if (course.getCode() == null) {
      violationMessages.add("Code is mandatory");
    }
    if (course.getName() == null) {
      violationMessages.add("Name is mandatory");
    }
    if (course.getCredits() < 0) {
      violationMessages.add("Credits must be positive");
    }
    if (course.getTotalHours() < 0) {
      violationMessages.add("Total hours must be positive");
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages = violationMessages.stream()
          .map(String::toString)
          .collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}