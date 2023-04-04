package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.model.User;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class CourseMapper {
  private final UserMapper userMapper;
  private final UserService userService;

  public Course toRest(school.hei.haapi.model.Course domain) {
    Course rest = new Course();
    Teacher mainTeacher = userMapper.toRestTeacher(domain.getMainTeacher());
    rest.setId(domain.getId());
    rest.setCode(domain.getCode());
    rest.setName(domain.getName());
    rest.setCredits(domain.getCredits());
    rest.setTotalHours(domain.getTotalHours());
    rest.setMainTeacher(mainTeacher);
    return rest;
  }

  public school.hei.haapi.model.Course toDomain(CrupdateCourse rest) {
    User mainTeacher = userService.getById(rest.getMainTeacherId());
    return school.hei.haapi.model.Course
        .builder()
        .id(rest.getId())
        .code(rest.getCode())
        .name(rest.getName())
        .credits(rest.getCredits())
        .totalHours(rest.getTotalHours())
        .mainTeacher(mainTeacher)
        .build();
  }
}
