package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;

import school.hei.haapi.repository.UserRepository;

@Component
@AllArgsConstructor
public class CourseMapper {
    private final UserMapper userMapper;
  private final UserRepository userRepository;

    public school.hei.haapi.endpoint.rest.model.Course toRestCourse(Course course){
        var restCourse = new school.hei.haapi.endpoint.rest.model.Course();
        restCourse.setId(course.getId());
        restCourse.setCode(course.getCode());
        restCourse.setName(course.getName());
        restCourse.setCredits(course.getCredits());
        restCourse.setTotalHours(course.getTotal_hours());
        restCourse.setMainTeacher(userMapper.toRestTeacher(course.getMain_teacher()));

        return restCourse;
    }

  public school.hei.haapi.endpoint.rest.model.Course toRest(Course course) {
    var restCourse = new school.hei.haapi.endpoint.rest.model.Course();
    restCourse.setId(course.getId());
    restCourse.setName(course.getName());
    restCourse.setCode(course.getCode());
    restCourse.setCredits(course.getCredits());
    restCourse.setTotalHours(course.getTotal_hours());
    restCourse.setMainTeacher(userMapper.toRestTeacher(course.getMain_teacher()));
    return restCourse;
  }

  public Course toDomain(school.hei.haapi.endpoint.rest.model.CrupdateCourse restCourse) {
    return Course.builder()
            .id(restCourse.getId())
            .name(restCourse.getName())
            .code(restCourse.getCode())
            .credits(restCourse.getCredits())
            .total_hours(restCourse.getTotalHours())
            .main_teacher(userRepository.findById(restCourse.getMainTeacherId()).get())
        .build();
  }
}
