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
  private final CourseRepository courseRepository;

  public List<Exam> getCourseExams(String courseId) {
    Course course = courseRepository.getCourseById(courseId);
    return examRepository.findExamsByCourse(course);
  }

  public Exam getExamById(String id) {
    return examRepository.getById(id);
  }
}
