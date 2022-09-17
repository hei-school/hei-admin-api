package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.service.CourseService;

@RestController
@AllArgsConstructor
public class CourseController {

  private final CourseService courseService;
  private final CourseMapper courseMapper;

  @GetMapping(value = "/courses/{id}")
  public Course getCourseById(@PathVariable String id) {
    return courseMapper.toRest(courseService.getById(id));
  }

  @GetMapping(value = "/courses")
  public List<Course> getCourses() {
    return courseService.getAll().stream()
        .map(courseMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @PutMapping(value = "/courses")
  public Course createOrUpdateCourse(@RequestBody Course toWrite) {
    return courseMapper.toRest(courseService.save(courseMapper.toDomain(toWrite)));
  }
}
