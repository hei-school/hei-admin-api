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
import school.hei.haapi.model.validator.GroupFlowValidator;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class GroupFlowService {

  private final GroupFlowRepository repository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final GroupFlowValidator validator;

  public GroupFlow save(CreateGroupFlow createGroupFlow) {
    Group group = groupRepository.getById(createGroupFlow.getGroupId());
    User student = userRepository.getById(createGroupFlow.getStudentId());
    GroupFlow groupFlow =
        GroupFlow.builder()
            .group(group)
            .flowDatetime(Instant.now())
            .student(student)
            .groupFlowType(
                GroupFlow.group_flow_type.fromValue(createGroupFlow.getMoveType().getValue()))
            .build();
    validator.accept(groupFlow);
    return repository.save(groupFlow);
  }

  @Transactional
  public List<GroupFlow> saveAll(List<CreateGroupFlow> createGroupFlows) {
    List<GroupFlow> groupFlows = new ArrayList<>();
    for (CreateGroupFlow createGroupFlow : createGroupFlows) {
      Group group = groupRepository.getById(createGroupFlow.getGroupId());
      User student = userRepository.getById(createGroupFlow.getStudentId());
      groupFlows.add(
          GroupFlow.builder()
              .group(group)
              .flowDatetime(Instant.now())
              .student(student)
              .groupFlowType(
                  GroupFlow.group_flow_type.fromValue(createGroupFlow.getMoveType().getValue()))
              .build());
    }
    validator.accept(groupFlows);
    return repository.saveAll(groupFlows);
  }
}
