package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.event.EventConf;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.endpoint.rest.security.model.Role;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.ForbiddenException;
import school.hei.haapi.service.UserService;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class StudentController {

  private final UserService userService;
  private final UserMapper userMapper;

  @GetMapping("/students/{id}")
  public Student getStudentById(
      @AuthenticationPrincipal Principal principal, @PathVariable String id) {
    if (Role.STUDENT.getRole().equals(principal.getRole())
        && !id.equals(principal.getUser().getId())) {
      throw new ForbiddenException();
    }
    return userMapper.toRestStudent(userService.getById(id));
  }

  @GetMapping("/students")
  public List<Student> getStudents(
      @RequestParam PageFromOne page, @RequestParam("page_size") BoundedPageSize pageSize) {
    return userService.getByRole(User.Role.STUDENT, page, pageSize).stream()
        .map(userMapper::toRestStudent)
        .collect(toUnmodifiableList());
  }

  @PutMapping("/students")
  public List<Student> saveAll(@RequestBody List<Student> toWrite) {
    if (toWrite.size() > EventConf.MAX_EVENT_REQUEST_ENTRY) {
      throw new BadRequestException("students to create or update must be ≤ 10");
    }
    return userService
        .saveAll(toWrite.stream().map(userMapper::toDomain).collect(toUnmodifiableList()))
        .stream()
        .map(userMapper::toRestStudent)
        .collect(toUnmodifiableList());
  }
}
