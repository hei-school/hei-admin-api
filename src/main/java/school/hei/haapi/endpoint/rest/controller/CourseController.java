package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;
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
    private final CourseMapper mapper

    @GetMapping(value = "/courses")
    public List<Course> getCourses(
            @RequestParam(name = "page", required = false, defaultValue = "1") PageFromOne page,
            @RequestParam(name = "page_size", required = false, defaultValue = "15") BoundedPageSize pageSize
    ){
        return service.getCourses(page, pageSize).stream()
                .map(mapper::toRest)
                .collect(Collectors.toUnmodifiableList());
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

  @PutMapping("/students/{student_id}/courses")
  public List<Course> updateStudentCourses(@PathVariable String studentId, @RequestBody List<UpdateStudentCourse> toWrite) {
     return service
        .saveAll(toWrite
            .stream()
            .map(updateStudentCourse -> mapper.toStudentCourseDomain(studentId, updateStudentCourse))
            .collect(toUnmodifiableList()))
        .stream()
        .map(courseMapper::toRest)
        .collect(toUnmodifiableList());
  }
}
