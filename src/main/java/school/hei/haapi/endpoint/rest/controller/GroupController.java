package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.GroupMapper;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.service.GroupService;

import static java.util.stream.Collectors.toUnmodifiableList;

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
  public List<Group> createOrUpdateGroups(@RequestBody List<Group> toWrite) {
    var saved = groupService.saveAll(toWrite.stream()
        .map(groupMapper::toDomain)
        .collect(toUnmodifiableList()));
    return saved.stream()
        .map(groupMapper::toRest)
        .collect(toUnmodifiableList());
  }
}
