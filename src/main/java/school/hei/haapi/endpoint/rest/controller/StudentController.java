package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.security.model.ApiClient;
import school.hei.haapi.endpoint.rest.security.model.Role;
import school.hei.haapi.model.exception.ForbiddenException;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class StudentController {

  private final UserService userService;
  private final UserMapper userMapper;

  @GetMapping(value = "/students/{id}")
  public Student getStudentById(
      @AuthenticationPrincipal ApiClient client, @PathVariable String id) {
    var student = userService.getStudentById(id);
    String clientRole = client.getRole();
    if (Role.STUDENT.getRole().equals(clientRole)) {
      var principalStudent = userService.getStudentByUserId(client.getUserId());
      if (!student.getId().equals(principalStudent.getId())) {
        throw new ForbiddenException("Students can only read their own information");
      }
    }

    return userMapper.toRestStudent(student);
  }
}
