package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.model.Course;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class CourseMapper {
  private final UserMapper userMapper;

  public school.hei.haapi.endpoint.rest.model.Course toRest(Course domain) {
    Teacher actualTeacher = domain.getMainTeacher() != null ?
        userMapper.toRestTeacher(domain.getMainTeacher()) : null;
    return new school.hei.haapi.endpoint.rest.model.Course()
        .id(domain.getId())
        .code(domain.getCode())
        .name(domain.getName())
        .credits(domain.getCredits())
        .totalHours(domain.getTotalHours())
        .mainTeacher(actualTeacher);
  }
}
