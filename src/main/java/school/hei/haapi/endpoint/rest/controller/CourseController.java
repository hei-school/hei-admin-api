package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.OrderDirection;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

@RestController
@AllArgsConstructor
public class CourseController {
  private final CourseService courseService;
  private final CourseMapper courseMapper;

  @GetMapping("/courses")
  public List<Course> getAllCourses(
          @RequestParam(required = false) PageFromOne page,
          @RequestParam(name = "page_size", required = false) BoundedPageSize pageSize,
          @RequestParam(name = "code" ,required = false) String code,
          @RequestParam(name = "name" ,required = false) String name,
          @RequestParam(name = "teacher_first_name" ,required = false) String teacherFirstName,
          @RequestParam(name = "teacher_last_name" ,required = false) String teacherLastName,
          @RequestParam(name = "credits", required = false) Integer credits,
          @RequestParam(name = "creditsOrder", required = false) OrderDirection creditsOrder,
          @RequestParam(name = "codeOrder", required = false) OrderDirection codeOrder
  ) {
    return courseService.getAllCourses(page, pageSize, code, name, teacherFirstName,
                    teacherLastName, credits, creditsOrder, codeOrder).stream().map(courseMapper::toRest)
            .collect(Collectors.toUnmodifiableList());
  }
}