package school.hei.haapi.service;

<<<<<<< HEAD
=======
import java.util.List;
>>>>>>> dcbf6c0e198524dab51d5515d0b92cabb24a51cc
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;

<<<<<<< HEAD
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

  public List<Course> getCoursesByStudentId(String studentId){
    return repository.getByStudentId(studentId);
=======
@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository repository;

  public List<Course> crupdateCourses(List<Course> courses){
    return repository.saveAll(courses);
>>>>>>> dcbf6c0e198524dab51d5515d0b92cabb24a51cc
  }
}
