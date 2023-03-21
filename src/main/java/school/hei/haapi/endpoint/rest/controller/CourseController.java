package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.endpoint.rest.validator.UpdateStudentCourseValidator;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.service.CourseService;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class CourseController {
  private final CourseService service;
  private final CourseMapper courseMapper;
  private final UpdateStudentCourseValidator validator;


  @GetMapping(value = "/courses")
  public List<Course> getGroups(
          @RequestParam(name = "page") PageFromOne page,
          @RequestParam(name = "page_size") BoundedPageSize pageSize
          ) {
    return service.getCourses(page,pageSize).stream()
            .map(courseMapper::toRest)
            .collect(toUnmodifiableList());
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
