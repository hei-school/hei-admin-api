package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.security.model.ApiClient;
import school.hei.haapi.endpoint.rest.security.model.Role;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ForbiddenException;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class TeacherController {

  private final UserService userService;
  private final UserMapper userMapper;

  @GetMapping(value = "/teachers/{id}")
  public Teacher getTeacherById(
      @AuthenticationPrincipal ApiClient client, @PathVariable String id) {
    User user = userService.getById(id);
    String clientRole = client.getRole();
    String userRole = user.getRole();
    if (Role.STUDENT.getRole().equals(clientRole)) {
      throw new ForbiddenException("Students are not allowed to access teacher information");
    }
    if (Role.TEACHER.getRole().equals(clientRole)
        && !id.equals(client.getUserId())
        && !Role.STUDENT.getRole().equals(userRole)) {
      throw new ForbiddenException(
          "A teacher cannot access information of another teacher");
    }
    return userMapper.toRestTeacher(user);
  }
}
