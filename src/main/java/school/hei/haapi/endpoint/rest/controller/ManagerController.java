package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Manager;
import school.hei.haapi.model.User;
import school.hei.haapi.service.UserService;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class ManagerController {

  private final UserService userService;
  private final UserMapper userMapper;

  @GetMapping(value = "/managers/{id}")
  public Manager getManagerById(@PathVariable String id) {
    return userMapper.toRestManager(userService.getById(id));
  }

  @GetMapping(value = "/managers")
  public List<Manager> getManagers() {
    return userService.getByRole(User.Role.MANAGER).stream()
        .map(userMapper::toRestManager)
        .collect(toUnmodifiableList());
  }
}
