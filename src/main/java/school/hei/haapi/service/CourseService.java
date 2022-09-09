package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository courseRepository;

  public List<Course> getAll(){
    return courseRepository.findAll();
  }
  public Course getCourseById (String id){
    Course course = courseRepository.getById(id);
    return course;
  }

  public Course saveCourse (Course course) {
    return courseRepository.save(course);
  }

}