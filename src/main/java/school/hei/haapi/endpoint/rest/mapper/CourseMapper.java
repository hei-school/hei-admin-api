package school.hei.haapi.endpoint.rest.mapper;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.model.CourseFollowed;
import school.hei.haapi.model.CourseFollowedRest;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.UserRepository;

@Component
@AllArgsConstructor

public class CourseMapper {

  private final UserMapper userMapper;
  private final CourseRepository courseRepository;
  private final UserRepository userRepository;

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

  public CourseFollowed toRest(CourseFollowedRest rest, String studentId){

    school.hei.haapi.model.Course course = courseRepository.getById(rest.getCourse_id());
    User student = userRepository.getById(studentId);

    return CourseFollowed.builder()
            .course(course)
            .student(student)
            .status(rest.getStatus())
            .build();
  }

}
