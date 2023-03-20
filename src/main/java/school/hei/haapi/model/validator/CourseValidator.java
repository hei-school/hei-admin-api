package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class CourseValidator implements Consumer<CrupdateCourse> {
  public void accept(List<CrupdateCourse> crupdateCourses) {
    crupdateCourses.forEach(this::accept);
  }

  @Override
  public void accept(CrupdateCourse crupdateCourse) {
    Set<String> violationMessages = new HashSet<>();

    if (crupdateCourse.getCode() == null) {
      violationMessages.add("Code is mandatory");
    }
    if(crupdateCourse.getCredits() == null){
      violationMessages.add("Credit is mandatory");
    }
    if(crupdateCourse.getTotalHours() == null){
      violationMessages.add("Total hours is mandatory");
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages = violationMessages.stream()
          .map(String::toString)
          .collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
