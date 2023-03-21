package school.hei.haapi.endpoint.rest.mapper;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.model.User;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class CourseMapper {
  private final CourseService service;
  private final UserService userService;
  private final UserMapper userMapper;

  public Course toRest(school.hei.haapi.model.Course domain) {
    Course course = new Course();
    Teacher mainTeacher =
        userMapper.toRestTeacher(userService.getById(domain.getMainTeacher().getId()));
    course.setId(domain.getId());
    course.setCode(domain.getCode());
    course.setCredits(domain.getCredits());
    course.setName(domain.getName());
    course.setTotalHours(domain.getTotalHours());
    course.setMainTeacher(mainTeacher);
    return course;
  }

  public school.hei.haapi.model.Course toDomain(CrupdateCourse rest) {
    User domainTeacher = userService.getById(rest.getMainTeacherId());
    if (service.getById(rest.getId()) == null
        && service.getByCode(rest.getCode()) == null) {
      return school.hei.haapi.model.Course
          .builder()
          .id(String.valueOf(UUID.randomUUID()))
          .code(rest.getCode())
          .credits(rest.getCredits())
          .totalHours(rest.getTotalHours())
          .mainTeacher(domainTeacher)
          .name(rest.getName())
          .build();
    }
    return school.hei.haapi.model.Course
        .builder()
        .id(rest.getId())
        .mainTeacher(domainTeacher)
        .code(rest.getCode())
        .credits(rest.getCredits())
        .name(rest.getName())
        .totalHours(rest.getTotalHours())
        .build();
  }

}