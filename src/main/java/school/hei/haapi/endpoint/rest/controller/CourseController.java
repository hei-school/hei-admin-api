package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.endpoint.rest.validator.UpdateStudentCourseValidator;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.service.CourseService;

@RestController
@AllArgsConstructor
public class CourseController {
  private final CourseService service;
  private final CourseMapper courseMapper;
  private final UpdateStudentCourseValidator validator;

  @GetMapping("students/{student_id}/courses")
  public List<Course> getStudentCourse(@PathVariable(name = "student_id") String student_id,
                                       @PathVariable (name = "status", required = false) CourseStatus status){
    return service.getStudentCourses(student_id, status).stream().map(courseMapper::toRest).collect(Collectors.toUnmodifiableList());
  }

  @PutMapping("students/{student_id}/courses")
  public List<Course> updateStudentCourse(
      @PathVariable(name = "student_id") String studentId,
      @RequestBody List<UpdateStudentCourse> toUpdate
  ) {
    toUpdate.forEach(validator);
    List<StudentCourse> toBeUpdated = toUpdate.stream()
        .map(course -> courseMapper.toDomain(studentId, course))
        .collect(Collectors.toUnmodifiableList());
    return service.updateStudentCourse(toBeUpdated).stream()
        .map(courseMapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }
}
