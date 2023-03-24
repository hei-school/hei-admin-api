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
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.endpoint.rest.validator.UpdateStudentCourseValidator;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.service.CourseService;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class CourseController {
  private final CourseService service;
  private final CourseMapper courseMapper;
  private final UpdateStudentCourseValidator validator;
  @PutMapping("courses")
  public List<Course> createOrUpdateCourse (
      @RequestBody List<CrupdateCourse> toCrupdate
  ){
    List<school.hei.haapi.model.Course> toSave = toCrupdate.stream().map(courseMapper::toDomainCourse)
            .collect(Collectors.toUnmodifiableList());
    return service.createOrUpdateCourse(toSave).stream().map(courseMapper::toRest)
            .collect(Collectors.toUnmodifiableList());
  }


  @GetMapping(value = "/courses")
  public List<Course> getCourses(
      @RequestParam(value = "code", defaultValue = "") String code,
      @RequestParam(value = "name", defaultValue = "") String name,
      @RequestParam(value = "credits", defaultValue = "") Integer credits,
      @RequestParam(value = "teacherFirstName", defaultValue = "") String teacherFirstName,
      @RequestParam(value = "teacherLastName", defaultValue = "") String teacherLastName,
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize,
      @RequestParam(name = "creditsOrder") String creditsOrder,
      @RequestParam(name = "codeOrder") String codeOrder
  ) {
    return service.getCourses(code, name, credits, teacherFirstName, teacherLastName,page, pageSize, creditsOrder, codeOrder).stream()
        .map(courseMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @GetMapping("students/{student_id}/courses")
  public List<Course> getStudentCourse(@PathVariable(name = "student_id") String student_id,
                                       @PathVariable(name = "status", required = false)
                                       CourseStatus status) {
    return service.getStudentCourses(student_id, status).stream().map(courseMapper::toRest)
        .collect(Collectors.toUnmodifiableList());
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
