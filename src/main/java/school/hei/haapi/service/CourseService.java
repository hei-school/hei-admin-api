package school.hei.haapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.mapper.AwardedCourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseDirection;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.User;
import school.hei.haapi.model.validator.CourseValidator;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.AwardedCourseRepository;
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
  private final AwardedCourseRepository awardedCourseRepository;
  private final GroupService groupService;
  private final AwardedCourseMapper awardedCourseMapper;

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
  public List<Course> createOrUpdateCourses(List<Course> toCreateOrUpdate) {
    courseValidator.accept(toCreateOrUpdate);
    return courseRepository.saveAll(toCreateOrUpdate);
  }

  public List<Course> getCoursesByTeacherId(String teacherId) {
    User teacher = userRepository.getById(teacherId);
    List<AwardedCourse> awardedCourses = teacher.getAwardedCourses();
    return awardedCourses.stream().map(awardedCourse -> awardedCourse.getCourse()).distinct().collect(Collectors.toList());
  }

  public List<Course> getCoursesByStudentId(String studentId) {
    List<Group> groups = groupService.getByUserId(studentId);
    List<AwardedCourse> awardedCourses = new ArrayList<>();
    for (Group group:groups) {
      awardedCourses.addAll(group.getAwardedCourse());
    }
    return awardedCourses.stream().map(awardedCourse -> awardedCourse.getCourse()).distinct().collect(Collectors.toList());
  }

  public List<Course> getCoursesByUserId(String userId) {
    User user = userRepository.getById(userId);
    switch(user.getRole()) {
      case MANAGER:
        return courseRepository.findAll();
      case TEACHER:
        return getCoursesByTeacherId(userId);
      case STUDENT:
        return getCoursesByStudentId(userId);
      default:
        return new ArrayList<>();
    }

  }
  public Course getById(String id) {
    return courseRepository.getCourseById(id);
  }

}
