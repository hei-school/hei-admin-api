package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseTemplate;
import school.hei.haapi.service.CourseService;

import java.util.List;

@RestController
@AllArgsConstructor
public class CourseController {
  private final CourseMapper mapper;
  private final CourseService service;

  @PutMapping("/students/{id}/courses")
  public List<Course> changeReferencedStudentCourse(
          @PathVariable String id,
          @RequestBody List<CourseTemplate> courseTemplate
          ){
  return service.updateStudentCourse(mapper.toList(id,courseTemplate));
  }
}
