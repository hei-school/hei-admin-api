package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.validator.CourseValidator;
import school.hei.haapi.repository.CourseRepository;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository courseRepository;

  private final CourseValidator courseValidator;

  public List<Course> getAll() {
    return courseRepository.findAll();
  }

  public Course getById(String id) {
    return courseRepository.getById(id);
  }

  public Course save(Course course) {
    courseValidator.accept(course);
    return courseRepository.save(course);
  }
}
