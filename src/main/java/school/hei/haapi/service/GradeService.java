package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class GradeService {
  private final GradeRepository gradeRepository;
  public List<Grade> getAllGradesBy(String id) {
    return gradeRepository.getGradeById(id);
  }

  public List<Grade> getGradeByStudentCourse(StudentCourse studentCourse) {
    return gradeRepository.getGradeByStudentCourse(studentCourse);
  }
  public List<Grade> getGradeByExam(Exam exam) {
    return gradeRepository.getGradeByExamId(exam);
  }

  public Grade getGradeByExamId(String id) {
    return gradeRepository.getGradeByExamId(id);
  }
}

