package school.hei.haapi.web.rest;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.exception.ForbiddenException;
import school.hei.haapi.mapper.UserMapper;
import school.hei.haapi.model.User;
import school.hei.haapi.security.model.ApiClient;
import school.hei.haapi.security.model.Role;
import school.hei.haapi.service.UserService;
import school.hei.haapi.web.model.TeacherResource;

@RestController
@AllArgsConstructor
public class TeacherController {

  private final UserService userService;
  private final UserMapper userMapper;

  @GetMapping(value = "/teachers/{id}")
  public TeacherResource getTeacherById(
      @AuthenticationPrincipal ApiClient client, @PathVariable String id) {
    User user = userService.getById(id);
    String clientRole = client.getRole();
    String userRole = user.getRole();
    if (Role.STUDENT.getRole().equals(clientRole)) {
      throw new ForbiddenException("Students are not allowed to access to this content");
    }
    if (Role.TEACHER.getRole().equals(clientRole)
        && !id.equals(client.getUserId())
        && !Role.STUDENT.getRole().equals(userRole)) {
      throw new ForbiddenException(
          "Teachers can only get their own information and students information");
    }
    return userMapper.toExternalTeacher(user);
  }
}
