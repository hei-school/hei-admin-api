package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;

@AllArgsConstructor
@Component
public class CourseMapper {
  private final UserMapper userMapper;
  public Course toRest(school.hei.haapi.model.Course course){
    return new Course()
            .id(course.getId())
            .credits(course.getCredits())
            .name(course.getName())
            .totalHours(course.getTotal_hours())
            .code(course.getCode())
            .mainTeacher(userMapper.toRestTeacher(course.getMain_teacher()));
  }
}
/**
 * export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
 * export PATH="$PATH:$JAVA_HOME"/bin
 */