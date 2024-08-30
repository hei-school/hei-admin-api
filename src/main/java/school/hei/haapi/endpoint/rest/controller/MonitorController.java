package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateMonitor;
import school.hei.haapi.endpoint.rest.model.Monitor;
import school.hei.haapi.endpoint.rest.validator.CoordinatesValidator;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class MonitorController {
  private final UserService userService;
  private final UserMapper userMapper;
  private final CoordinatesValidator validator;

  @GetMapping(value = "/monitors/{id}")
  public Monitor getMonitorById(@PathVariable String id) {
    return userMapper.toRestMonitor(userService.findById(id));
  }

  @PutMapping("/monitors/{id}")
  public Monitor updateMonitorById(
      @PathVariable(name = "id") String monitorId, @RequestBody CrupdateMonitor toUpdate) {
    validator.accept(toUpdate.getCoordinates());
    return userMapper.toRestMonitor(
        userService.updateUser(userMapper.toDomain(toUpdate), monitorId));
  }
}
