package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;

@Component
@AllArgsConstructor
public class CourseMapper {
  public Course toDomain(school.hei.haapi.endpoint.rest.model.Course course) {
    return Course.builder()
        .id(course.getId())
        .ref(course.getRef())
        .name(course.getName())
        .credits(course.getCredits())
        .total_hours(course.getTotalHours())
        .build();
  }

  public school.hei.haapi.endpoint.rest.model.Course toRest(Course course) {
    school.hei.haapi.endpoint.rest.model.Course restCourse =
        new school.hei.haapi.endpoint.rest.model.Course();
    restCourse.setId(course.getId());
    restCourse.setRef(course.getRef());
    restCourse.setName(course.getName());
    restCourse.setCredits(course.getCredits());
    restCourse.setTotalHours(course.getTotal_hours());
    return restCourse;
  }
}
