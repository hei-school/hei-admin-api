package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;
import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository courseRepository;

  public List<Course> findAllCourse() {
    return courseRepository.findAll();
  }
  public Course findByCourseId (String id) {
    return courseRepository.getById(id);
  }
  @Transactional
  public Course updateCourse (Course courseList) {
    return courseRepository.save(courseList);
  }
}