package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.model.Grade;
import school.hei.haapi.repository.GradeRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class GradeService {
  // todo: to review all class
  private final GradeRepository gradeRepository;

  public Grade getGradeByExamIdAndStudentId(String examId, String studentId) {
    return gradeRepository.getGradeByExamIdAndStudentIdAndAwardedCourseIdAndGroupId(
        examId, studentId);
  }

  public List<Grade> saveAll(List<Grade> grades) {
    return gradeRepository.saveAll(grades);
  }
}
