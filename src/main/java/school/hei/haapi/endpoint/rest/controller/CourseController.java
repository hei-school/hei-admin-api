package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseDirection;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

@RestController
@AllArgsConstructor
public class CourseController {
  private final CourseService service;
  private final CourseMapper mapper;

  @GetMapping("/courses")
  public List<Course> getCourses(
      @RequestParam(value = "code", required = false, defaultValue = "") String code,
      @RequestParam(value = "name", required = false, defaultValue = "") String name,
      @RequestParam(value = "credits", required = false) Integer credits,
      @RequestParam(value = "teacher_first_name", required = false, defaultValue = "")
      String teacherFirstName,
      @RequestParam(value = "teacher_last_name", required = false, defaultValue = "")
      String teacherLastName,
      @RequestParam(value = "credits_order", defaultValue = "")
      CourseDirection creditsOrder,
      @RequestParam(value = "code_order", defaultValue = "")
      CourseDirection codeOrder,
      @RequestParam(defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize
  ) {
    return service.getCourses(code, name, credits, creditsOrder, codeOrder,
            teacherFirstName, teacherLastName, page, pageSize)
        .stream()
        .map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }

  @PutMapping("/courses")
  public List<Course> crupdateCourses(@RequestBody List<CrupdateCourse> courses) {
    return service.crupdateCourses(courses
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toUnmodifiableList()))
        .stream()
        .map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }

  @GetMapping("students/{student_id}/courses")
  public List<Course> getStudentCoursesById(
      @PathVariable("student_id") String studentId,
      @RequestParam(value = "status", required = false) CourseStatus status) {
    return service.getCoursesByStatus(studentId, status)
        .stream()
        .map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }

  @PutMapping("students/{student_id}/courses")
  public List<Course> updateStudentCourses(
      @PathVariable("student_id") String studentId,
      @RequestBody List<UpdateStudentCourse> studentCourses) {
    return service.updateStudentCourses(studentId, studentCourses)
        .stream()
        .map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }

}
