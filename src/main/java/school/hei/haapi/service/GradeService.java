package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Grade;
import school.hei.haapi.repository.GradeRepository;

@Service
@AllArgsConstructor
public class GradeService {
  private final GradeRepository gradeRepository;
  public Grade getGradeByExamIdAndStudentId(String examId,
                                            String studentId,
                                            String awardedCourseId,
                                            String groupId) {
    return gradeRepository.getGradeByExamIdAndStudentIdAndAwardedCourseIdAndGroupId(examId,
        awardedCourseId, groupId, studentId);
  }
}

