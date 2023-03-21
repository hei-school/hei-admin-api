package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {

  private final CourseRepository repository;

  public Course getById(String groupId) {
    return repository.getById(groupId);
  }

  public List<Course> getAll() {
    return repository.findAll();
  }

  public List<Course> saveAll(List<Course> groups) {
    return repository.saveAll(groups);
  }
}
