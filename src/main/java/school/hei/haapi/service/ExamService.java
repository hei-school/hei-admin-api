package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Exam;
import school.hei.haapi.repository.ExamRepository;

@Service
@AllArgsConstructor
public class ExamService {
  private final ExamRepository examRepository;

  public List<Exam> getCourseExams(String courseId) {
    return examRepository.findByCourseId(courseId);
  }

  public Exam getExamById(String id) {
    return examRepository.getById(id);
  }
}
