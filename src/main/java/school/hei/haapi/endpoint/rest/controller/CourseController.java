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
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
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

  @PutMapping("/courses")
  public List<Course> crupdateCourse(
      @RequestBody List<CrupdateCourse> toCrupdate){
    return courseService.crupdateCourse(toCrupdate).stream().map(
        courseMapper::toRest
    ).collect(Collectors.toUnmodifiableList());
  }

  @PutMapping("/students/{student_id}/courses")
  public List<Course> updateStudentCourseLink(
          @PathVariable(name = "student_id") String studentId ,
          @RequestBody List <UpdateStudentCourse> courseToUpdate
          ){
    return courseService.updateStudentCourseLink(courseToUpdate.stream().map(
        rest -> courseMapper.toDomain(rest, studentId)
        ).collect(Collectors.toUnmodifiableList()), studentId)
            .stream()
            .map(courseMapper::toRest).collect(Collectors.toUnmodifiableList());
  }
}
