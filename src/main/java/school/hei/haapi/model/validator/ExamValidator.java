package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateGrade;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.ExamService;

@Component
@AllArgsConstructor
public class ExamValidator implements Consumer<Exam> {
  private final ExamService examService;

  public void accept(List<Exam> exams) {
    exams.forEach(this::accept);
  }

  @Override
  public void accept(Exam exam) {
    Set<String> violationMessages = new HashSet<>();
    if (exam.getCoefficient() < 0) {
      violationMessages.add("Coefficient can't be less than 0");
    }
    if (exam.getTitle() == null) {
      violationMessages.add("Title is mandatory");
    }
    if (exam.getAwardedCourse() == null) {
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

  public void validateExamId(String examId, List<CreateGrade> createGrades) {
    Set<String> violationMessages = new HashSet<>();
    if (!examService.examExistsById(examId)) {
      violationMessages.add("Exam with this id does not exist");
    }
    if (createGrades != null && !createGrades.isEmpty()) {
      boolean allMatch =
          createGrades.stream().allMatch(createGrade -> examId.equals(createGrade.getExamId()));
      if (!allMatch) {
        violationMessages.add("Exam ids in the payload do not match the id in the URL");
      }
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages =
          violationMessages.stream().map(String::toString).collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
