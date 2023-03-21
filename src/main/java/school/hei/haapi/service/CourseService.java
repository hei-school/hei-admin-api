package school.hei.haapi.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.StudentCourseRepository;

@Service
@AllArgsConstructor
public class CourseService {
  private final StudentCourseRepository repository;
  private final CourseRepository courseRepository;
  public List<Course> createOrUpdateCourse(List<Course> toUpdate) {
    return courseRepository.saveAll(toUpdate);
  }


  public List<Course> updateStudentCourse(List<StudentCourse> toUpdate) {
    return repository.saveAll(toUpdate).stream()
        .map(StudentCourse::getCourse)
        .collect(Collectors.toUnmodifiableList());
  }
}
