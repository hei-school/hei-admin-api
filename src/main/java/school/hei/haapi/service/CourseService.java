package school.hei.haapi.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.StudentCourseRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.CourseDao;

@Service
@AllArgsConstructor
public class CourseService {
  private final StudentCourseRepository repository;
  private final UserRepository userRepository;
  private final CourseDao courseDao;

  public List<Course> getStudentCourses(String studentId, CourseStatus status) {
    CourseStatus status1 = status == null ? CourseStatus.LINKED: status;
    if(userRepository.findById(studentId).isPresent()){
      return repository.findByStudentIdAndStatus(studentId, status1).stream()
              .map(StudentCourse::getCourse)
              .collect(Collectors.toUnmodifiableList());
    }
    throw new NotFoundException("Student." + studentId + " is not found.");
  }
  private final CourseRepository courseRepository;
  public List<Course> createOrUpdateCourse(List<Course> toUpdate) {
    return courseRepository.saveAll(toUpdate);
  }



  public List<Course> getCourses(PageFromOne page, BoundedPageSize pageSize, String code, String name, Integer credits,
                                 String teacherFirstName, String teacherLastName) {
    int pageValue = page.getValue() - 1;
    int pageSizeValue = pageSize.getValue();
    Pageable pageable = PageRequest.of(pageValue, pageSizeValue);
    return courseDao.findByCriteria(code, name, credits, teacherFirstName, teacherLastName, pageable);
  }

  public List<Course> updateStudentCourse(List<StudentCourse> toUpdate) {
    return repository.saveAll(toUpdate).stream()
        .map(StudentCourse::getCourse)
        .collect(Collectors.toUnmodifiableList());
  }
}
