package school.hei.haapi.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.model.CourseDirection;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.User;
import school.hei.haapi.model.validator.CourseValidator;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.StudentCourseRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.CourseDao;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static school.hei.haapi.endpoint.rest.model.CourseStatus.LINKED;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseDao courseDao;
  private final CourseRepository courseRepository;
  private final CourseValidator courseValidator;
  private final UserRepository userRepository;
  private final StudentCourseRepository studentCourseRepository;

  public List<Course> getCourses(
      String code, String name, Integer credits, CourseDirection creditsOrder,
      CourseDirection codeOrder, String teacherFirstName, String teacherLastName,
      PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(ASC, "name"));
    return courseDao.findByCriteria(code, name, credits,
        teacherFirstName, teacherLastName, String.valueOf(creditsOrder),
        String.valueOf(codeOrder), pageable);
  }

  @Transactional
  public List<Course> crupdateCourses(List<Course> toCrupdate) {
    courseValidator.accept(toCrupdate);
    return courseRepository.saveAll(toCrupdate);
  }

  public List<Course> getCoursesByStatus(String userId, CourseStatus status) {
    if (status == null) {
      return courseDao.getByUserIdAndStatus(userId, LINKED);
    }
    return courseDao.getByUserIdAndStatus(userId, status);
  }

  public Course getById(String id) {
    return courseRepository.getCourseById(id);
  }

  @Transactional
  public List<Course> updateStudentCourses(String userId, List<UpdateStudentCourse> toUpdate) {
    User toRetrieve = userRepository.getById(userId);
    return toUpdate.stream()
        .map(update -> updateCourseStatus(toRetrieve, update))
        .collect(Collectors.toList());
  }

  private Course updateCourseStatus(User user, UpdateStudentCourse update) {
    Course course = courseRepository.getById(update.getCourseId());
    StudentCourse studentCourse =
        studentCourseRepository.findByStudentIdAndCourseId(user.getId(), course.getId());
    if (studentCourse == null) {
      studentCourse = StudentCourse
          .builder()
          .student(user)
          .course(course)
          .build();
    }
    studentCourse.setStatus(update.getStatus());
    studentCourseRepository.save(studentCourse);
    return course;
  }

}
