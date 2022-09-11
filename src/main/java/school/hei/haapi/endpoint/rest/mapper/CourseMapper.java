package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;

@Component
public class CourseMapper {

  public Course toRest(school.hei.haapi.model.Course restCourse) {
    return new Course()
            .id(restCourse.getId())
            .ref(restCourse.getRef())
            .name(restCourse.getName())
            .totalHours(restCourse.getTotalHours())
            .credits(restCourse.getCredits());
  }
  public school.hei.haapi.model.Course toDomain (Course domainCourse) {
    return school.hei.haapi.model.Course.builder()
            .id(domainCourse.getId())
            .credits(domainCourse.getCredits())
            .name(domainCourse.getName())
            .ref(domainCourse.getRef())
            .totalHours(domainCourse.getTotalHours())
            .build();
  }
}