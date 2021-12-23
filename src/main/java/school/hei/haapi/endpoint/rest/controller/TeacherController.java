package school.hei.haapi.endpoint.rest.controller;

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

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class TeacherController {

  private final UserService userService;
  private final UserMapper userMapper;

  @GetMapping(value = "/teachers/{id}")
  public Teacher getTeacherById(
      @AuthenticationPrincipal Principal principal, @PathVariable String id) {
    String principalRole = principal.getRole();
    if (Role.STUDENT.getRole().equals(principalRole)) {
      throw new ForbiddenException("Students cannot read teachers");
    }
    if (Role.TEACHER.getRole().equals(principalRole) && !id.equals(principal.getUserId())) {
      throw new ForbiddenException("Teachers can only read their own information");
    }

    User user = userService.getById(id);
    return userMapper.toRestTeacher(user);
  }

  @GetMapping(value = "/teachers")
  public List<Teacher> getTeachers(@AuthenticationPrincipal Principal principal) {
    if (!Role.MANAGER.getRole().equals(principal.getRole())) {
      throw new ForbiddenException("Only managers can read all teachers");
    }

    return userService.getByRole(User.Role.TEACHER).stream()
        .map(userMapper::toRestTeacher)
        .collect(toUnmodifiableList());
  }

  @PutMapping(value = "/teachers")
  public List<Teacher> createOrUpdateTeachers(
      @AuthenticationPrincipal Principal principal, @RequestBody List<Teacher> toWrite) {
    if (!Role.MANAGER.getRole().equals(principal.getRole())) {
      throw new ForbiddenException("Only managers can write teachers");
    }

    var saved = userService.saveAll(toWrite.stream()
        .map(userMapper::toDomain)
        .collect(toUnmodifiableList()));
    return saved.stream()
        .map(userMapper::toRestTeacher)
        .collect(toUnmodifiableList());
  }
}
