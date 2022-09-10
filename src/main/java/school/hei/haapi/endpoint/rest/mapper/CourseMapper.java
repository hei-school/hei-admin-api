package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;
@Component
public class CourseMapper {

  public Course toRest(school.hei.haapi.model.Course domain){
    return new Course()
        .id(domain.getId())
        .name(domain.getName())
        .ref(domain.getRef())
        .credits(domain.getCredits())
        .totalHours(domain.getTotalHours()) ;

  }
  public school.hei.haapi.model.Course toDomain(Course rest){
    return school.hei.haapi.model.Course.builder()
        .id(rest.getId())
        .name(rest.getName())
        .ref(rest.getRef())
        .credits(rest.getCredits())
        .totalHours(rest.getTotalHours())
        .build();
  }

}
