package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.*;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.GroupMapper;
import school.hei.haapi.endpoint.rest.model.CreateGroup;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
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
  public List<Group> getGroups(
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize) {
    return groupService.getAll(page, pageSize).stream()
        .map(groupMapper::toRest)
        .collect(toUnmodifiableList());
  }

  // todo: to review
  @PutMapping(value = "/groups")
  public List<Group> createOrUpdateGroups(@RequestBody List<CreateGroup> createGroupsRest) {
    List<school.hei.haapi.model.notEntity.CreateGroup> createGroups =
        createGroupsRest.stream().map(groupMapper::toDomain).collect(toList());

    var saved = groupService.saveAll(createGroups);
    return saved.stream().map(groupMapper::toRest).collect(toUnmodifiableList());
  }
}
