package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.security.model.ApiClient;
import school.hei.haapi.endpoint.rest.security.model.Role;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ForbiddenException;
import school.hei.haapi.service.UserService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class StudentController {

  private final UserService userService;
  private final UserMapper userMapper;

  @GetMapping(value = "/students/{id}")
  public Student getStudentById(
      @AuthenticationPrincipal ApiClient client, @PathVariable String id) {
    if (Role.STUDENT.getRole().equals(client.getRole())) {
      if (!id.equals(client.getUser().getId())) {
        throw new ForbiddenException("Students can only read their own information");
      }
    }

    return userMapper.toRestStudent(userService.getById(id));
  }

  @GetMapping(value = "/students")
  public List<Teacher> getStudents(@AuthenticationPrincipal ApiClient client) {
    if (Role.STUDENT.getRole().equals(client.getRole())) {
      throw new ForbiddenException("Students can only read their own information");
    }

    return userService.getByRole(User.Role.STUDENT).stream()
        .map(userMapper::toRestTeacher)
        .collect(toUnmodifiableList());
  }

}
