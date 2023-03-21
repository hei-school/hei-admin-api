package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.controller.response.UpdateStudentCourseStatusResponse;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.model.*;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.StudentCourseRepository;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.StudentCourseService;
import school.hei.haapi.service.UserService;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class StudentController {
  private final UserService userService;
  private final UserMapper userMapper;
  private final CourseMapper courseMapper;
  private final StudentCourseService studentCourseService;

  @GetMapping("/students/{id}")
  public Student getStudentById(@PathVariable String id) {
    return userMapper.toRestStudent(userService.getById(id));
  }

  @GetMapping("/students")
  public List<Student> getStudents(
      @RequestParam PageFromOne page, @RequestParam("page_size") BoundedPageSize pageSize,
      @RequestParam(value = "ref", required = false, defaultValue = "") String ref,
      @RequestParam(value = "first_name", required = false, defaultValue = "") String firstName,
      @RequestParam(value = "last_name", required = false, defaultValue = "") String lastName) {
    return userService.getByCriteria(User.Role.STUDENT, firstName, lastName, ref, page, pageSize
        ).stream()
        .map(userMapper::toRestStudent)
        .collect(toUnmodifiableList());
  }

  @PutMapping("/students")
  public List<Student> saveAll(@RequestBody List<Student> toWrite) {
    return userService
        .saveAll(toWrite
            .stream()
            .map(userMapper::toDomain)
            .collect(toUnmodifiableList()))
        .stream()
        .map(userMapper::toRestStudent)
        .collect(toUnmodifiableList());
  }

  @PutMapping("/students/{student_id}/courses")
  public List<school.hei.haapi.endpoint.rest.model.Course> updateStatus(@PathVariable String student_id, @RequestBody List<UpdateStudentCourseStatusResponse> updateStudentCourseStatusResponse) {
    return
            studentCourseService.updateStatus(student_id,updateStudentCourseStatusResponse)
                    .stream()
                    .map(courseMapper::toRest)
                    .collect(toUnmodifiableList());
  }

}
