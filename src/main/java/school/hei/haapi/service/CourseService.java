package school.hei.haapi.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.repository.StudentCourseRepository;

@Service
@AllArgsConstructor
public class CourseService {
  private final StudentCourseRepository repository;

  public List<Course> getStudentCourses(String studentId, CourseStatus status) {
    CourseStatus status1 = status == null ? CourseStatus.LINKED: status;
    return repository.findByStudentIdAndStatus(studentId, status1).stream().map(StudentCourse::getCourse).collect(Collectors.toUnmodifiableList());
  }

  public List<Course> updateStudentCourse(List<StudentCourse> toUpdate) {
    return repository.saveAll(toUpdate).stream()
        .map(StudentCourse::getCourse)
        .collect(Collectors.toUnmodifiableList());
  }
}
