package school.hei.haapi.endpoint.rest.mapper;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.CourseFollowed;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.model.CourseFollowedRest;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.UserRepository;

@Component
@AllArgsConstructor

public class CourseMapper {

  private UserMapper userMapper;
  private UserRepository userRepository;
  private final CourseRepository courseRepository;

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


  public school.hei.haapi.model.Course toDomain(CrupdateCourse rest){
    return school.hei.haapi.model.Course.builder()
        .idCourse(rest.getId())
        .teacher(userRepository.getById(rest.getMainTeacherId()))
        .code(rest.getCode())
        .name(rest.getName())
        .credits(rest.getCredits())
        .totalHours(rest.getTotalHours())
        .build();
  }

  public school.hei.haapi.model.CourseFollowed toDomain(UpdateStudentCourse rest, String studentId){
    school.hei.haapi.model.Course course = courseRepository.getById(rest.getCourseId());
    User student = userRepository.getById(studentId);

    return CourseFollowed.builder()
        .course(course)
        .student(student)
        .status(rest.getStatus())
        .build();
  }
}
