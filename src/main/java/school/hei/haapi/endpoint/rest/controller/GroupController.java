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
  public Group getGroupById(@PathVariable String id) {
    return groupMapper.toRest(groupService.getById(id));
  }

  @GetMapping(value = "/groups")
  public List<Group> getGroups() {
    return groupService.getAll().stream()
        .map(groupMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @PutMapping(value = "/groups")
  public List<Group> createOrUpdateGroups(
      @AuthenticationPrincipal ApiClient client, @RequestBody List<Group> newRestGroups) {
    String clientRole = client.getRole();
    if (!Role.MANAGER.getRole().equals(clientRole)) {
      throw new ForbiddenException("Only managers can write groups");
    }
    var createdGroups = groupService.saveAll(newRestGroups.stream()
        .map(groupMapper::toDomain)
        .collect(toUnmodifiableList()));
    return createdGroups.stream()
        .map(groupMapper::toRest)
        .collect(toUnmodifiableList());
  }
}
