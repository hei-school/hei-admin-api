package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.endpoint.rest.security.model.Role;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ForbiddenException;
import school.hei.haapi.service.UserService;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class TeacherController {

  private final UserService userService;
  private final UserMapper userMapper;

  @GetMapping(value = "/teachers/{id}")
  public Teacher getTeacherById(
      @AuthenticationPrincipal Principal principal, @PathVariable String id) {
    if (Role.TEACHER.getRole().equals(principal.getRole()) && !id.equals(principal.getUserId())) {
      throw new ForbiddenException();
    }
    return userMapper.toRestTeacher(userService.getById(id));
  }

  @GetMapping(value = "/teachers")
  public List<Teacher> getTeachers() {
    return userService.getByRole(User.Role.TEACHER).stream()
        .map(userMapper::toRestTeacher)
        .collect(toUnmodifiableList());
  }

  @PutMapping(value = "/teachers")
  public List<Teacher> createOrUpdateTeachers(@RequestBody List<Teacher> toWrite) {
    var saved = userService.saveAll(toWrite.stream()
        .map(userMapper::toDomain)
        .collect(toUnmodifiableList()));
    return saved.stream()
        .map(userMapper::toRestTeacher)
        .collect(toUnmodifiableList());
  }
}
