package school.hei.haapi.model.validator;

import java.util.Optional;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.GradeRepository;

@Component
@AllArgsConstructor
public class IsNewGradeChecker implements Consumer<Grade> {
  private final GradeRepository gradeRepository;

  @Override
  public void accept(Grade grade) {
    String examId = grade.getExam().getId();
    String studentId = grade.getStudent().getId();

    existGradeOfStudentInExam(examId, studentId);
  }

  private void existGradeOfStudentInExam(String examId, String studentId) {
    Optional<Grade> existingGrade = gradeRepository.findByExamIdAndStudentId(examId, studentId);
    if (existingGrade.isPresent()) {
      String error =
          String.format(
              "Grade for the student %s for the exam %s already exist", studentId, examId);
      throw new BadRequestException(error);
    }
  }
}
