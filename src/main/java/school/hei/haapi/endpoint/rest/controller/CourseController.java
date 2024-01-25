package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseDirection;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

@RestController
@AllArgsConstructor
public class CourseController {
  private final CourseService service;
  private final CourseMapper mapper;

  // todo: to review all class
  @GetMapping("/courses")
  public List<Course> getCourses(
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "credits", required = false) Integer credits,
      @RequestParam(value = "teacher_first_name", required = false) String teacherFirstName,
      @RequestParam(value = "teacher_last_name", required = false) String teacherLastName,
      @RequestParam(value = "credits_order", required = false) CourseDirection creditsOrder,
      @RequestParam(value = "code_order", required = false) CourseDirection codeOrder,
      @RequestParam(defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize) {
    return service
        .getCourses(
            code,
            name,
            credits,
            creditsOrder,
            codeOrder,
            teacherFirstName,
            teacherLastName,
            page,
            pageSize)
        .stream()
        .map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }

  @PutMapping("/courses")
  public List<Course> createOrUpdateCourses(@RequestBody List<Course> courses) {
    return service
        .createOrUpdateCourses(
            courses.stream().map(mapper::toDomain).collect(Collectors.toUnmodifiableList()))
        .stream()
        .map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }

  @GetMapping("/courses/{course_id}")
  public Course getCoursesById(@PathVariable("course_id") String courseId) {
    return mapper.toRest(service.getById(courseId));
  }

  @DeleteMapping("/courses/{courseId}")
  public Course deleteCourseById(@PathVariable("courseId") String courseId) {
    return mapper.toRest(service.deleteCourseById(courseId));
  }
}
