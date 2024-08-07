package school.hei.haapi.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import school.hei.haapi.repository.UserRepository;

@Slf4j
@Service
@AllArgsConstructor
public class GroupFlowService {

  private final GroupFlowRepository repository;
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final GroupFlowValidator validator;

  private void logger(GroupFlow studentGroupFlow) {
    log.info(
        "student = "
            + studentGroupFlow.getStudent().toString()
            + " "
            + studentGroupFlow.getGroupFlowType()
            + " group = "
            + studentGroupFlow.getGroup().toString());
  }

  private User findUserById(String userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new NotFoundException("User with id." + userId + " not found"));
  }

  private Group findGroupById(String groupId) {
    return groupRepository
        .findById(groupId)
        .orElseThrow(() -> new NotFoundException("Group with id." + groupId + " not found"));
  }

  public GroupFlow save(CreateGroupFlow createGroupFlow) {
    GroupFlow groupFlowToSave = fromCreateGroupFlowsToGroupFlows(createGroupFlow);

    validator.accept(groupFlowToSave);
    logger(groupFlowToSave);
    return repository.save(groupFlowToSave);
  }

  @Transactional
  public List<GroupFlow> saveAll(List<CreateGroupFlow> createGroupFlows) {
    List<GroupFlow> groupFlowsToSave =
        createGroupFlows.stream()
            .map(this::fromCreateGroupFlowsToGroupFlows)
            .collect(Collectors.toList());

    validator.accept(groupFlowsToSave);
    groupFlowsToSave.forEach(this::logger);
    return repository.saveAll(groupFlowsToSave);
  }

  private GroupFlow fromCreateGroupFlowsToGroupFlows(CreateGroupFlow toMap) {
    return GroupFlow.builder()
        .student(findUserById(toMap.getStudentId()))
        .group(findGroupById(toMap.getGroupId()))
        .flowDatetime(Instant.now())
        .groupFlowType(GroupFlow.GroupFlowType.fromValue(toMap.getMoveType().getValue()))
        .build();
  }
}
