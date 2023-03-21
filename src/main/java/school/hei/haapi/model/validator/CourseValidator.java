package school.hei.haapi.model.validator;

import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class CourseValidator implements Consumer<Course> {

    @Override
    public void accept(Course course) {
        Set<String> violationMessages = new HashSet<>();
        if (course.getName() == null) {
            violationMessages.add("name is mandatory");
        }
        if (course.getCode() == null) {
            violationMessages.add("code is mandatory");
        }
        if (course.getCredits() < 0) {
            violationMessages.add("credits must be positive");
        }
        if (course.getTotalHours() < 0) {
            violationMessages.add("total hours must be positive");
        }
        if (!violationMessages.isEmpty()) {
            String formattedViolationMessages = violationMessages.stream()
                    .map(String::toString)
                    .collect(Collectors.joining(". "));
            throw new BadRequestException(formattedViolationMessages);
        }
    }

    public void accept(List<Course> courses) {
        courses.forEach(this::accept);
    }
}
