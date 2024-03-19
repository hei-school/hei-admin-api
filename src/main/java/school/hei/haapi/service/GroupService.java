package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.model.CreateGroupFlow;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class GroupService {
  // todo: to review all class

  private final GroupRepository repository;
  private final UserRepository userRepository;
  private final GroupFlowService groupFlowService;

  public Group getById(String groupId) {
    return repository.getById(groupId);
  }

  public List<Group> getAllById(List<String> groupsId) {
    try {
      return repository.findAllById(groupsId);
    } catch (Exception e) {
      throw new NotFoundException(e.getMessage());
    }
  }

  public List<Group> getAll(PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return repository.findAll(pageable).toList();
  }

  @Transactional
  public List<Group> saveAll(List<school.hei.haapi.model.notEntity.CreateGroup> createGroups) {
    List<school.hei.haapi.model.Group> groups = new ArrayList<>();
    List<CreateGroupFlow> createGroupFlows = new ArrayList<>();
    for (school.hei.haapi.model.notEntity.CreateGroup createGroup : createGroups) {
      Group group = repository.save(createGroup.getGroup());
      groups.add(group);
      if (createGroup.getStudentsToAdd() != null) {
        for (String studentId : createGroup.getStudentsToAdd()) {
          createGroupFlows.add(
              new CreateGroupFlow()
                  .moveType(CreateGroupFlow.MoveTypeEnum.JOIN)
                  .groupId(group.getId())
                  .studentId(studentId));
        }
      }
    }
    groupFlowService.saveAll(createGroupFlows);
    return groups;
  }

  public List<Group> getByUserId(String studentId) {
    User student = userRepository.getById(studentId);
    List<Group> groups = new ArrayList<>();
    List<GroupFlow> groupFlows = new ArrayList<>();
    for (GroupFlow groupFlow : student.getGroupFlows()) {
      if (!groups.contains(groupFlow.getGroup())) {
        if (groupFlows.stream()
                .filter(groupFlow1 -> groupFlow1.getGroup() == groupFlow.getGroup())
                .count()
            > 0) {
          if (groupFlow
              .getFlowDatetime()
              .isAfter(
                  groupFlows.stream()
                      .filter(groupFlow1 -> groupFlow1.getGroup().equals(groupFlow.getGroup()))
                      .findFirst()
                      .get()
                      .getFlowDatetime())) {
            if (groupFlow.getGroupFlowType() == GroupFlow.GroupFlowType.JOIN) {
              groups.add(groupFlow.getGroup());
            }
            groupFlows.remove(
                groupFlows.stream()
                    .filter(groupFlow1 -> groupFlow1.getGroup().equals(groupFlow.getGroup()))
                    .findFirst()
                    .get());
            groupFlows.add(groupFlow);
          }
        } else {
          if (groupFlow.getGroupFlowType() == GroupFlow.GroupFlowType.JOIN) {
            groups.add(groupFlow.getGroup());
          }
          groupFlows.add(groupFlow);
        }
      } else if (groupFlow
          .getFlowDatetime()
          .isAfter(
              groupFlows.stream()
                  .filter(groupFlow1 -> groupFlow1.getGroup().equals(groupFlow.getGroup()))
                  .findFirst()
                  .get()
                  .getFlowDatetime())) {
        if (groupFlow.getGroupFlowType() == GroupFlow.GroupFlowType.LEAVE) {
          groups.remove(groupFlow.getGroup());
        }
        groupFlows.remove(
            groupFlows.stream()
                .filter(groupFlow1 -> groupFlow1.getGroup().equals(groupFlow.getGroup()))
                .findFirst()
                .get());
        groupFlows.add(groupFlow);
      }
    }
    return groups;
  }
}
