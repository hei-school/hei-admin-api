package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.validator.CreateFeeValidator;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.UserService;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toUnmodifiableList;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.UNPAID;

@Component
@AllArgsConstructor
public class CourseMapper {

  private final UserService userService;
  private final CreateFeeValidator createFeeValidator;
  private final UserMapper userMapper;

  public Course toRestCourse(school.hei.haapi.model.Course course) {
    Teacher teacher = userMapper.toRestTeacher(course.getMainTeacher());
    return new Course()
            .id(course.getId())
            .code(course.getCode())
            .name(course.getName())
            .credits(course.getCredits())
            .mainTeacher(teacher)
            .totalHours(course.getTotalHours());
  }

  public school.hei.haapi.model.Course toDomain(Course course){
    User user = userMapper.toDomain(course.getMainTeacher());
    return school.hei.haapi.model.Course.builder()
            .id(course.getId())
            .code(course.getCode())
            .totalHours(course.getTotalHours())
            .mainTeacher(user)
            .name(course.getName())
            .build();
  }
}
