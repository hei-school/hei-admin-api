package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.OrderDirection;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.service.CourseService;

@RestController
@AllArgsConstructor
public class CourseController {
  private final CourseService service;
  private final CourseMapper mapper;

  @GetMapping(value = "/courses")
  public List<Course> getCourses(
      @RequestParam(name = "creditsOrder", required = false) OrderDirection creditsOrder,
      @RequestParam(name = "codeOrder", required = false) OrderDirection codeOrder,
      @RequestParam(name = "page", required = false, defaultValue = "1") PageFromOne page,
      @RequestParam(name = "page_size", required = false, defaultValue = "15") BoundedPageSize pageSize
  ){
    return service.getAllCourses(creditsOrder, codeOrder,page, pageSize).stream()
        .map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }

  @GetMapping(value = "/courses/{id}")
  public Course getCourseById (@PathVariable String id){
    return mapper.toRest(service.getCourseById(id));
  }
  @PutMapping("/courses")
  public List<Course> crupdateCourse(@RequestBody List<CrupdateCourse> toCrupdate) {
    List<school.hei.haapi.model.Course> saved = service.crupdateCourses(
        toCrupdate.stream()
            .map(mapper::toDomain)
            .collect(Collectors.toUnmodifiableList())
    );
    return saved.stream()
        .map(mapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }
}