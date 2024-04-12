package school.hei.haapi.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.model.CreateGroupFlow;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.GroupFlowValidator;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;

@Service
@AllArgsConstructor
public class GroupFlowService {

  private final GroupFlowRepository repository;
  private final GroupRepository groupRepository;
  private final UserService userService;
  private final GroupFlowValidator validator;

  private Group findGroupById(String groupId) {
    return groupRepository
        .findById(groupId)
        .orElseThrow(() -> new NotFoundException("Group with id." + groupId + " not found"));
  }

  public GroupFlow save(CreateGroupFlow createGroupFlow) {
    Group group = findGroupById(createGroupFlow.getStudentId());
    User student = userService.findById(createGroupFlow.getStudentId());

    GroupFlow toSave =
        GroupFlow.builder()
            .group(group)
            .flowDatetime(Instant.now())
            .student(student)
            .groupFlowType(
                GroupFlow.GroupFlowType.fromValue(createGroupFlow.getMoveType().getValue()))
            .build();
    validator.accept(toSave);
    return repository.save(toSave);
  }

  @Transactional
  public List<GroupFlow> saveAll(List<CreateGroupFlow> createGroupFlows) {
    List<GroupFlow> groupFlows = new ArrayList<>();

    for (CreateGroupFlow createGroupFlow : createGroupFlows) {
      Group group = findGroupById(createGroupFlow.getStudentId());
      User student = userService.findById(createGroupFlow.getStudentId());

      groupFlows.add(
          GroupFlow.builder()
              .group(group)
              .flowDatetime(Instant.now())
              .student(student)
              .groupFlowType(
                  GroupFlow.GroupFlowType.fromValue(createGroupFlow.getMoveType().getValue()))
              .build());
    }
    validator.accept(groupFlows);
    return repository.saveAll(groupFlows);
  }
}
