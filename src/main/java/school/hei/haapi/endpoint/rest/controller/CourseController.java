package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.Order;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class CourseController {
  private final CourseService service;
  private final CourseMapper courseMapper;


  @GetMapping(value = "/courses")
  public List<Course> getCourses(
      @RequestParam(name = "code", required = false) String code,
      @RequestParam(name = "name", required = false) String name,
      @RequestParam(name = "credits", required = false) Integer credits,
      @RequestParam(name = "teacher_first_name", required = false) String teacherFirstname,
      @RequestParam(name = "teacher_last_name", required = false) String teacherLastName,
      @RequestParam(name = "creditsOrder", required = false) Order creditsOrder,
      @RequestParam(name = "codeOrder", required = false) Order codeOrder,
      @RequestParam(name = "page", required = false) PageFromOne page,
      @RequestParam(name = "page_size", required = false) BoundedPageSize pageSize
  ) {
    return service.getCourses(code, name, credits, teacherFirstname, teacherLastName, page,
            pageSize, creditsOrder, codeOrder).stream()
        .map(courseMapper::toRest)
        .collect(toUnmodifiableList());
  }

}
