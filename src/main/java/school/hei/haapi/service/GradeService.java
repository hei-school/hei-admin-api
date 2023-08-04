package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class GradeService {
  private final GradeRepository gradeRepository;
  private final UserRepository userRepository;
  public List<Grade> getAllGradesByExamId(String examId) {
    return gradeRepository.getGradesByExam_Id(examId);
  }

  public List<Grade> getGradesByStudentCourse(StudentCourse studentCourse) {
    return gradeRepository.getGradesByStudentCourse(studentCourse);
  }

  public Grade getGradeByExamIdAndStudentId(String examId, String studentId) {
    return gradeRepository.getGradeByExamIdAndStudentCourseStudent(examId, userRepository.getById(studentId));
  }
}

