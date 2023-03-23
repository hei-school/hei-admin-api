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
import school.hei.haapi.model.CodeEnum;
import school.hei.haapi.model.CreditEnum;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;


@RestController
@AllArgsConstructor
public class CourseController {
  private final CourseMapper mapper;

  private final CourseService service;

  @GetMapping("/courses")
  public List<Course> getCourses(@RequestParam(value = "page", defaultValue = "1") PageFromOne page,
                                 @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize,
                                 @RequestParam(value = "code", required = false, defaultValue = "") String code,
                                 @RequestParam(value = "name", required = false, defaultValue = "") String name,
                                 @RequestParam(value = "credits",required = false) Integer credits,
                                 @RequestParam(value = "teacher_first_name", required = false, defaultValue = "") String teacher_first_name,
                                 @RequestParam(value = "teacher_last_name", required = false, defaultValue = "") String teacher_last_name
                                 ) {
    return service.getCourses(page, pageSize, code, name, credits, teacher_first_name, teacher_last_name)
        .stream()
        .map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }

  @GetMapping("/courses/{id}")
  public Course getCourseById(@PathVariable String id) {
    return mapper.toRest(service.getById(id));
  }

  @GetMapping("/students/{student_id}/courses")
  public List<school.hei.haapi.model.Course> getStudentCoursesById(
      @PathVariable("student_id") String studentId,
      @RequestParam(value = "status", defaultValue = "LINKED") CourseStatus status
  ) {
    if (status.equals(CourseStatus.UNLINKED)) {
      return service.getUnlinkedCoursesByStudentId(studentId);
    }
    return service.getLinkedCoursesByStudentId(studentId);
  }

  @PutMapping("/courses")
  public List<Course> crupdateCourses(@RequestBody List<CrupdateCourse> toWrite) {
    return service.crupdateCourses(toWrite
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toUnmodifiableList()))
        .stream()
        .map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }


}
