package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.CourseFollowedRest;
import school.hei.haapi.service.CourseService;

@RestController
@AllArgsConstructor
public class CourseController {

  private final CourseService courseService;
  private final CourseMapper courseMapper;

  @GetMapping("/courses")
  public List<Course> getAllCourses(
      @RequestParam(required = false) PageFromOne page,
      @RequestParam(name = "page_size", required = false) BoundedPageSize pageSize
  ) {
    return courseService.getAllCourses(page, pageSize).stream().map(courseMapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }

  @GetMapping("/students/{student_id}/courses")
  public List<Course> getCourseFollowedByOneStudent(
      @PathVariable(name = "student_id") String studentId,
      @RequestParam(name = "status", required = false, defaultValue = "LINKED") CourseStatus status
  ) {
    return courseService.getCourseFollowedByOneStudent(studentId, status).stream()
        .map(courseMapper::toRest).collect(Collectors.toUnmodifiableList());
  }

  @PutMapping("/students/{student_id}/courses")
  public List<Course> updateStudentCourseLink(
          @PathVariable(name = "student_id") String studentId ,
          @RequestBody List <CourseFollowedRest> courseToUpdate
          ){
    return courseService.updateStudentCourseLink(courseToUpdate, studentId)
            .stream()
            .map(courseMapper::toRest).collect(Collectors.toUnmodifiableList());
  }
}
