package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Course;

@Component
@AllArgsConstructor
public class CourseMapper {
  // todo: to review all class
  public Course toRest(school.hei.haapi.model.Course domain) {
    return new Course()
        .id(domain.getId())
        .code(domain.getCode())
        .name(domain.getName())
        .credits(domain.getCredits())
        .totalHours(domain.getTotalHours())
        .level(domain.getStudentLevel());
  }

  public school.hei.haapi.model.Course toDomain(Course rest) {
    return school.hei.haapi.model.Course.builder()
        .id(rest.getId())
        .code(rest.getCode())
        .name(rest.getName())
        .credits(rest.getCredits())
        .totalHours(rest.getTotalHours())
        .studentLevel(rest.getLevel())
        .build();
  }

  public List<Course> toRests(List<school.hei.haapi.model.Course> domains) {
    return domains.stream().map(this::toRest).toList();
  }
}
