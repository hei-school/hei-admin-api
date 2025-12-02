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
    Double score = grade.getScore();

    existGradeOfStudentInExam(examId, studentId, score);
  }

  private void existGradeOfStudentInExam(String examId, String studentId, Double score) {
    Optional<Grade> existingGrade = gradeRepository.findByExamIdAndStudentId(examId, studentId);
    if (existingGrade.isPresent() && existingGrade.get().getScore().equals(score)) {
      String error =
          String.format(
              "Grade for the student %s for the exam %s already exist and have a same score %s",
              studentId, examId, score);
      throw new BadRequestException(error);
    }
  }
}
