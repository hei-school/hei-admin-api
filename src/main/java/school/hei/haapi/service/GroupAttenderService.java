package school.hei.haapi.service;

import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.GroupAttender;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.GroupAttenderRepository;
import school.hei.haapi.repository.dao.GroupAttenderDao;

@Service
@AllArgsConstructor
public class GroupAttenderService {
  private final GroupAttenderRepository groupAttenderRepository;
  private final GroupAttenderDao groupAttenderDao;

  public List<GroupAttender> getAllByGroupId(String groupId) {
    return groupAttenderRepository.findAllByGroupId(groupId);
  }

  public List<GroupAttender> getAllByStudentId(String studentId) {
    return groupAttenderRepository.findAllByStudentId(studentId);
  }

  public List<GroupAttender> getGroupAttenderByGroupIdAndStudentCriteria(
      String groupId,
      String studentRef,
      String studentLastname,
      String studentFirstname,
      PageFromOne page,
      BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());

    return groupAttenderDao.findAllByGroupIdAndStudentCriteria(
        groupId, studentRef, studentLastname, studentFirstname, pageable);
  }

  public List<GroupAttender> saveAll(List<GroupAttender> groupAttenders) {
    return groupAttenderRepository.saveAll(groupAttenders);
  }

  private void deleteAll(List<GroupAttender> groupAttendersToDelete) {
    groupAttendersToDelete.forEach(
        attender -> {
          groupAttenderRepository.deleteByStudentIdAndGroupId(
              attender.getStudent().getId(), attender.getGroup().getId());
        });
  }

  public List<GroupAttender> saveFromGroupFlows(List<GroupFlow> groupFlows) {
    List<GroupAttender> joinGroupFlows =
        filterByGroupFlowsType(GroupFlow.GroupFlowType.JOIN, groupFlows).stream()
            .map(this::fromGroupFlow)
            .collect(toList());
    List<GroupAttender> leaveGroupFlows =
        filterByGroupFlowsType(GroupFlow.GroupFlowType.LEAVE, groupFlows).stream()
            .map(this::fromGroupFlow)
            .collect(toList());

    deleteAll(leaveGroupFlows);
    return saveAll(joinGroupFlows);
  }

  private GroupAttender fromGroupFlow(GroupFlow groupFlow) {
    return GroupAttender.builder()
        .id(groupFlow.getId())
        .group(groupFlow.getGroup())
        .student(groupFlow.getStudent())
        .migrationDatetime(groupFlow.getFlowDatetime())
        .build();
  }

  public List<GroupFlow> filterByGroupFlowsType(
      GroupFlow.GroupFlowType type, List<GroupFlow> groupFlows) {
    return groupFlows.stream()
        .filter(groupFlow -> type.equals(groupFlow.getGroupFlowType()))
        .collect(toList());
  }
}
