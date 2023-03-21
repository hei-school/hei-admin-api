package school.hei.haapi.endpoint.rest.mapper;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.model.CourseFollowed;

@Component
@AllArgsConstructor

public class CourseMapper {

  private UserMapper userMapper;

  public Course toRest(CourseFollowed domain){
    return new Course()
        .id(domain.getCourse().getIdCourse())
        .name(domain.getCourse().getName())
        .credits(domain.getCourse().getCredits())
        .totalHours(domain.getCourse().getTotalHours())
        .code(domain.getCourse().getCode())
        .mainTeacher(userMapper.toRestTeacher(domain.getCourse().getTeacher()));
  }

  public Course toRest(school.hei.haapi.model.Course domain){
    return new Course()
        .id(domain.getIdCourse())
        .name(domain.getName())
        .credits(domain.getCredits())
        .totalHours(domain.getTotalHours())
        .code(domain.getCode())
        .mainTeacher(userMapper.toRestTeacher(domain.getTeacher()));
  }
}
