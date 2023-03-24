package school.hei.haapi.endpoint.rest.mapper;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.repository.UserRepository;

@Component
@AllArgsConstructor

public class CourseMapper {

  private UserMapper userMapper;
  private UserRepository userRepository;


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