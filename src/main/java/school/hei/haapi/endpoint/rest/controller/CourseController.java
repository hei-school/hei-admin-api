package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.service.CourseService;

@RestController
@AllArgsConstructor
public class CourseController {

  private final CourseService courseService;
  private final CourseMapper courseMapper;

  @GetMapping("/students/{student_id}/courses")
  public List<Course> getCourseFollowedByOneStudent(
      @PathVariable(name = "student_id") String studentId,
      @RequestParam(name = "status", required = false, defaultValue = "LINKED") CourseStatus status
  ) {
    return courseService.getCourseFollowedByOneStudent(studentId, status).stream()
        .map(courseMapper::toRest).collect(Collectors.toUnmodifiableList());
  }
}
