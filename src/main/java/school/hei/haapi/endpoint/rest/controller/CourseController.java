package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.service.CourseService;

@RestController
@AllArgsConstructor
public class CourseController {
  private final CourseService service;
  private final CourseMapper mapper;

  @PutMapping("/courses")
  public List<Course> crupdateCourse(@RequestBody List<CrupdateCourse> toCrupdate) {
    List<school.hei.haapi.model.Course> saved = service.crupdateCourses(
        toCrupdate.stream()
            .map(mapper::toDomain)
            .collect(Collectors.toUnmodifiableList())
    );
    return saved.stream()
        .map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }
}
