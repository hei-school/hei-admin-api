package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class ExamValidator implements Consumer<Exam> {

  public void accept(List<Exam> exams) {
    exams.forEach(this);
  }

  @Override
  public void accept(Exam exam) {
    Set<String> violationMessages = new HashSet<>();
    if (exam.getCoefficientNumerator() != null || exam.getCoefficientDenominator() != null) {
      if (exam.getCoefficientNumerator() <= 0) {
        violationMessages.add("Coefficient numerator can't be less than 0");
      }
      if (exam.getCoefficientDenominator() <= 0) {
        violationMessages.add("Coefficient denominator can't be less than 0");
      }
    } else {
      violationMessages.add("Coefficient numerator or denominator cannot be null");
    }
    if (exam.getTitle() == null) {
      violationMessages.add("Title is mandatory");
    }
    if (exam.getCourseAssignment() == null) {
      violationMessages.add("Awarded course is mandatory");
    }
    if (exam.getExaminationDate() == null) {
      violationMessages.add("Examination date is mandatory");
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages =
          violationMessages.stream().map(String::toString).collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
