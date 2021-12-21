package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  public Group getGroupById(@AuthenticationPrincipal ApiClient client, @PathVariable String id) {
    checkReadAuthorization(client);
    return groupMapper.toRest(groupService.getById(id));
  }

  @GetMapping(value = "/groups")
  public List<Group> getGroups(@AuthenticationPrincipal ApiClient client) {
    checkReadAuthorization(client);
    return groupService.findAll().stream()
        .map(groupMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @PutMapping(value = "/groups")
  public List<Group> createOrUpdateGroups(
      @AuthenticationPrincipal ApiClient client, @RequestBody List<Group> newRestGroups) {
    checkWriteAuthorization(client);
    var createdGroups = groupService.saveAll(newRestGroups.stream()
        .map(groupMapper::toDomain)
        .collect(toUnmodifiableList()));
    return createdGroups.stream()
        .map(groupMapper::toRest)
        .collect(toUnmodifiableList());
  }

  private void checkReadAuthorization(ApiClient client) {
    String clientRole = client.getRole();
    if (!Role.STUDENT.getRole().equals(clientRole)
        && !Role.TEACHER.getRole().equals(clientRole)
        && !Role.MANAGER.getRole().equals(clientRole)) {
      throw new ForbiddenException("Only students/teachers/managers can read groups");
    }
  }

  private void checkWriteAuthorization(ApiClient client) {
    String clientRole = client.getRole();
    if (!Role.MANAGER.getRole().equals(clientRole)) {
      throw new ForbiddenException("Only managers can write groups");
    }
  }
}
