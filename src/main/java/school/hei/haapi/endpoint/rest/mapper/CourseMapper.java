package school.hei.haapi.endpoint.rest.mapper;

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
  public Course toRest(school.hei.haapi.model.Course domain){
    Teacher actualTeacher = domain.getMainTeacher() != null ?
        userMapper.toRestTeacher(domain.getMainTeacher()) : null;
    return new Course()
        .id(domain.getId())
        .code(domain.getCode())
        .name(domain.getName())
        .credits(domain.getCredits())
        .totalHours(domain.getTotalHours())
        .mainTeacher(userMapper.toRestTeacher(domain.getMainTeacher()));
  }
  public school.hei.haapi.model.Course toDomain(CrupdateCourse rest) {
    validator.accept(rest);
    User actualTeacher =
        rest.getMainTeacherId() == null ? null : userService.getById(rest.getMainTeacherId());
    String id = rest.getId() == null ? String.valueOf(UUID.randomUUID()) : rest.getId();
    return new school.hei.haapi.model.Course().toBuilder()
        .id(id)
        .code(rest.getCode())
        .name(rest.getName())
        .credits(rest.getCredits())
        .totalHours(rest.getTotalHours())
        .mainTeacher(actualTeacher)
        .build();
  }
}