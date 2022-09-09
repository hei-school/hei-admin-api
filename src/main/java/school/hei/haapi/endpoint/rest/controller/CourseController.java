package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.service.CourseService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class CourseController {

  private final CourseService courseService;

  private final CourseMapper courseMapper;

  @GetMapping("/courses/{id}")
  public Course getCourseById (@PathVariable String id){
    return courseMapper.toRest(courseService.getCourseById(id));
  }

  @GetMapping("/courses")
  public List<Course> getAllCourses (){
    return courseService.getAll()
        .stream()
        .map(courseMapper :: toRest)
        .collect(Collectors.toUnmodifiableList());
  }

  @PutMapping("/courses")
  public Course saveCourse (
      @RequestBody Course course
  ){
    var saved = courseMapper.toDomain(course);
    return courseMapper.toRest(courseService.saveCourse(saved));
  }
}
