package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.GroupMapper;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.security.model.ApiClient;
import school.hei.haapi.endpoint.rest.security.model.Role;
import school.hei.haapi.model.exception.ForbiddenException;
import school.hei.haapi.service.GroupService;

@RestController
@AllArgsConstructor
public class GroupController {

  private final GroupService groupService;
  private final GroupMapper groupMapper;

  @GetMapping(value = "/groups/{id}")
  public Group getGroupById(
      @AuthenticationPrincipal ApiClient client, @PathVariable String id) {
    String clientRole = client.getRole();
    if (!Role.STUDENT.getRole().equals(clientRole)
        && !Role.TEACHER.getRole().equals(clientRole)
        && !Role.MANAGER.getRole().equals(clientRole)) {
      throw new ForbiddenException("Only students/teachers/managers can get group info");
    }
    return groupMapper.toRest(groupService.getById(id));
  }
}
