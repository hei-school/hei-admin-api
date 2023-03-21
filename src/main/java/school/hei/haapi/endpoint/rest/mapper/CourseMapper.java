package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.UserService;

import java.util.List;
import java.util.Objects;

@Component
public class CourseMapper {

  private UserMapper userMapper;
  private UserService userService;
  private CourseService courseService;

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

  public Course toStudentCourseDomain(String studentId, school.hei.haapi.endpoint.rest.model.UpdateStudentCourse restStudentCourse) {
    Course domain = courseService.getById(restStudentCourse.getCourseId());
    User student = userService.getById(studentId);
    return Course.builder()
            .id(domain.getId())
            .code(domain.getCode())
            .name(domain.getName())
            .credits(domain.getCredits())
            .totals_hours(domain.getTotals_hours())
            .students(List.of(student))
            .main_teacher(domain.getMain_teacher())
            .status(restStudentCourse.getStatus())
            .build();
  }
}
