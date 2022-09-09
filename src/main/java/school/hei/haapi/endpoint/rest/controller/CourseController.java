package school.hei.haapi.endpoint.rest.controller;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.service.CourseService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@Controller
@RestController
@AllArgsConstructor
@RequestMapping("/courses")
@Data
public class CourseController {
  private final CourseService courseService;
  private final CourseMapper courseMapper;
  @GetMapping
  public List<Course> findAllCourses(

  ) {
    return courseService.findAllCourse()
            .stream()
            .map(courseMapper::toRest)
            .collect(toUnmodifiableList());
  }

  @GetMapping("/{id}")
  public school.hei.haapi.model.Course findCourseById (
          @PathVariable String id) {
    return courseService.findByCourseId(id);
  }

  @PutMapping
  public Course updateCourse (
          @RequestBody Course course)
  {
    var data = courseMapper.toDomain(course);
    return courseMapper.toRest(courseService.updateCourse(data)) ;
  }
}