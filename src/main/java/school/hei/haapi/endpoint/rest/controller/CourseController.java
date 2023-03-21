package school.hei.haapi.endpoint.rest.controller;

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
  }
}
