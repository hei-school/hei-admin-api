package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.CourseValidator;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.UserService;

import java.util.Objects;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.model.User;
import school.hei.haapi.model.validator.CourseValidator;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class CourseMapper {
  private final UserMapper userMapper;
  private final UserService userService;
  private final CourseValidator validator;
  private CourseService courseService;

  public Course toRest(school.hei.haapi.model.Course domain) {
    Teacher actualTeacher = domain.getMainTeacher() != null ?
        userMapper.toRestTeacher(domain.getMainTeacher()) : null;
    return new Course()
        .id(domain.getId())
        .code(domain.getCode())
        .name(domain.getName())
        .credits(domain.getCredits())
        .totalHours(domain.getTotalHours())
        .mainTeacher(actualTeacher);
  }

  public school.hei.haapi.model.Course toDomain(CrupdateCourse rest) {
    validator.accept(rest);
    User actualTeacher =
        rest.getMainTeacherId() == null ? null : userService.getById(rest.getMainTeacherId());
    String id = rest.getId() == null ? String.valueOf(UUID.randomUUID()) : rest.getId();
    return new school.hei.haapi.model.Course()
        .toBuilder()
        .id(id)
        .code(rest.getCode())
        .name(rest.getName())
        .credits(rest.getCredits())
        .totalHours(rest.getTotalHours())
        .mainTeacher(actualTeacher)
        .build();
  }

  public school.hei.haapi.endpoint.rest.model.Course toRest(Course course) {
    var restCourse = new school.hei.haapi.endpoint.rest.model.Course();
    restCourse.setId(course.getId());
    restCourse.setCode(course.getCode());
    restCourse.setName(course.getName());
    restCourse.setCredits(course.getCredits());
    restCourse.setTotalHours(course.getTotals_hours());
    restCourse.setMainTeacher(userMapper.toRestTeacher(course.getMain_teacher()));
    return restCourse;
  }
  public Course toDomain(school.hei.haapi.endpoint.rest.model.UpdateStudentCourse restCourse) {
    Course domain = courseService.getById(restCourse.getCourseId());
    return Course.builder()
            .id(domain.getId())
            .code(domain.getCode())
            .name(domain.getName())
            .credits(domain.getCredits())
            .totals_hours(domain.getTotals_hours())
            .main_teacher(domain.getMain_teacher())
            .status(restCourse.getStatus())
            .build();
   }
}
