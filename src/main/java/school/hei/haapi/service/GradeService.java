package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.AwardedCourse;
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


  //todo: modify for having scope
  public Grade getGradeByExamIdAndStudentId(String examId, String studentId, String awardedCourseId, String groupId) {
    return gradeRepository.getGradeByExamIdAndStudentIdAndAwardedCourseIdAndGroupId(examId, awardedCourseId, groupId, studentId);
  }
}

