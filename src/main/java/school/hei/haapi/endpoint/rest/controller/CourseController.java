package school.hei.haapi.endpoint.rest.controller;

<<<<<<< HEAD
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

@RestController
@AllArgsConstructor
public class CourseController {
  private final CourseService courseService;
  private final CourseMapper courseMapper;

  @PutMapping("/students/{student_id}/courses")
  public List<Course> updateStudentCourses(@PathVariable String studentId, @RequestBody List<UpdateStudentCourse> toWrite) {
     return courseService
        .saveAll(toWrite
            .stream()
            .map(updateStudentCourse -> courseMapper.toStudentCourseDomain(studentId, updateStudentCourse))
            .collect(toUnmodifiableList()))
        .stream()
        .map(courseMapper::toRest)
        .collect(toUnmodifiableList());
=======
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.service.CourseService;

@RestController
@AllArgsConstructor
public class CourseController {
  private final CourseService service;
  private final CourseMapper mapper;

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
>>>>>>> dcbf6c0e198524dab51d5515d0b92cabb24a51cc
  }
}
