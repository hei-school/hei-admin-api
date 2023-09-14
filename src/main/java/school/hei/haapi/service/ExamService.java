package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Exam;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.ExamRepository;

@Service
@AllArgsConstructor
public class ExamService {
  private final ExamRepository examRepository;
  public List<Exam> getExamsFromAwardedCourseIdAndGroupId(String groupId, String awardedCourseId) {
    return examRepository.findExamsByCourseIdAndAwardedGroupId(groupId, awardedCourseId);
  }

  public Exam getExamsByIdAndGroupIdAndAwardedCourseId(String id, String awardedCourseId, String groupId) {
    return examRepository.findExamsByIdAndGroupIdAndAwardedGroupId(id, awardedCourseId, groupId);
  }

  public List<Exam> updateOrSaveAll(List<Exam> exams) {
    return examRepository.saveAll(exams);
  }
}
