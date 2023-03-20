package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository repository;

  public List<Course> crupdateCourses(List<Course> courses){
    return repository.saveAll(courses);
  }
}
