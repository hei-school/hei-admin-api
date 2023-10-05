package school.hei.haapi.service;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class GroupService {

  private final GroupRepository repository;
  private final UserRepository userRepository;

  public Group getById(String groupId) {
    return repository.getById(groupId);
  }

  public List<Group> getAll(PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1,
            pageSize.getValue(),
            Sort.by(DESC, "creationDatetime"));
    return repository.getGroups(pageable).toList();
  }

  public List<Group> saveAll(List<Group> groups) {
    return repository.saveAll(groups);
  }

  public List<Group> getByUserId(String studentId) {
    User student = userRepository.getById(studentId);
    List<Group> groups = new ArrayList<>();
    List<GroupFlow> groupFlows = new ArrayList<>();
    for (GroupFlow groupFlow : student.getGroupFlows()) {
      if (!groups.contains(groupFlow.getGroup())) {
        if (groupFlows.stream().filter(groupFlow1 -> groupFlow1.getGroup() == groupFlow.getGroup())
            .count() > 0) {
          if (groupFlow.getFlowDatetime().isAfter(groupFlows.stream()
              .filter(groupFlow1 -> groupFlow1.getGroup().equals(groupFlow.getGroup())).findFirst()
              .get().getFlowDatetime())) {
            if (groupFlow.getGroupFlowType() == GroupFlow.group_flow_type.JOIN) {
              groups.add(groupFlow.getGroup());
            }
            groupFlows.remove(groupFlows.stream()
                .filter(groupFlow1 -> groupFlow1.getGroup().equals(groupFlow.getGroup()))
                .findFirst().get());
            groupFlows.add(groupFlow);
          }
        } else {
          if (groupFlow.getGroupFlowType() == GroupFlow.group_flow_type.JOIN) {
            groups.add(groupFlow.getGroup());
          }
          groupFlows.add(groupFlow);
        }
      } else if (groupFlow.getFlowDatetime().isAfter(groupFlows.stream()
          .filter(groupFlow1 -> groupFlow1.getGroup().equals(groupFlow.getGroup())).findFirst()
          .get().getFlowDatetime())) {
        if (groupFlow.getGroupFlowType() == GroupFlow.group_flow_type.LEAVE) {
          groups.remove(groupFlow.getGroup());
        }
        groupFlows.remove(groupFlows.stream()
            .filter(groupFlow1 -> groupFlow1.getGroup().equals(groupFlow.getGroup())).findFirst()
            .get());
        groupFlows.add(groupFlow);
      }
    }
    return groups;
  }
}
