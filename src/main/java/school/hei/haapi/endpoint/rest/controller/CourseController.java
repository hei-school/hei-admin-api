package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.model.BoundedPageSize;

import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @GetMapping("/courses")
    public List<Course> getAllCourse (
        @RequestParam(value = "page", required = false, defaultValue = "1") PageFromOne page,
        @RequestParam(value = "page_size", required = false, defaultValue = "15") BoundedPageSize pageSize,
        @RequestParam(value = "teacher_first_name", required = false) String teacher_first_name,
        @RequestParam(value = "teacher_last_name", required = false) String teacher_last_name) {
            return courseService.getByCriteria(page, pageSize,teacher_first_name, teacher_last_name)
                    .stream().map(courseMapper::toRestCourse)
                    .collect(toUnmodifiableList());
    }

  @PutMapping(value = "/courses")
  public List<Course> createOrUpdateCourses(@RequestBody List<CrupdateCourse> toWrite) {
    var saved = courseService.saveAll(toWrite.stream()
        .map(courseMapper::toDomain)
        .collect(toUnmodifiableList()));
    return saved.stream()
        .map(courseMapper::toRest)
        .collect(toUnmodifiableList());
  }
}
