package school.hei.haapi.service;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.data.domain.Sort.Direction.ASC;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.gen.UserUpserted;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.model.User;
import school.hei.haapi.model.validator.UserValidator;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.UserManagerDao;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final EventProducer eventProducer;
  private final UserValidator userValidator;
  private final UserManagerDao userManagerDao;
  private final GroupRepository groupRepository;
  private final GroupService groupService;

  public User getById(String userId) {
    return userRepository.getById(userId);
  }

  public User getByEmail(String email) {
    return userRepository.getByEmail(email);
  }

  @Transactional
  public List<User> saveAll(List<User> users) {
    userValidator.accept(users);
    List<User> savedUsers = userRepository.saveAll(users);
    eventProducer.accept(
        users.stream().map(this::toUserUpsertedEvent).collect(toUnmodifiableList()));
    return savedUsers;
  }

  private UserUpserted toUserUpsertedEvent(User user) {
    return new UserUpserted().userId(user.getId()).email(user.getEmail());
  }

  public List<User> getByRole(User.Role role, PageFromOne page, BoundedPageSize pageSize, EnableStatus status, Sex sex) {
    return getByCriteria(role, "", "", "", page, pageSize, status, sex);
  }

  public List<User> getByCriteria(
      User.Role role,
      String firstName,
      String lastName,
      String ref,
      PageFromOne page,
      BoundedPageSize pageSize,
      EnableStatus status,
      Sex sex) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "ref"));
    return userManagerDao.findByCriteria(role, ref, firstName, lastName, pageable, status, sex);
  }

  public List<User> getByLinkedCourse(
      User.Role role,
      String firstName,
      String lastName,
      String ref,
      String courseId,
      PageFromOne page,
      BoundedPageSize pageSize,
      EnableStatus status,
      Sex sex) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "ref"));
    List<User> users = userManagerDao.findByCriteria(role, ref, firstName, lastName, pageable, status, sex);

    return courseId.length() > 0
        ? users.stream()
            .filter(
                user ->
                    groupService.getByUserId(user.getId()).stream()
                        .anyMatch(
                            group ->
                                group.getAwardedCourse().stream()
                                    .anyMatch(
                                        awardedCourse ->
                                            awardedCourse.getCourse().getId().equals(courseId))))
            .collect(toList())
        : users;
  }

  public List<User> getByGroupId(String groupId) {
    Group group = groupRepository.getById(groupId);
    List<User> users = new ArrayList<>();
    List<GroupFlow> groupFlows = new ArrayList<>();
    for (GroupFlow groupFlow : group.getGroupFlows()) {
      if (!users.contains(groupFlow.getStudent())) {
        if (groupFlow.getGroupFlowType() == GroupFlow.group_flow_type.JOIN) {
          users.add(groupFlow.getStudent());
        }
        groupFlows.add(groupFlow);
      } else if (groupFlow
          .getFlowDatetime()
          .isAfter(
              groupFlows.stream()
                  .filter(groupFlow1 -> groupFlow1.getStudent().equals(groupFlow.getStudent()))
                  .findFirst()
                  .get()
                  .getFlowDatetime())) {
        if (groupFlow.getGroupFlowType() == GroupFlow.group_flow_type.LEAVE) {
          users.remove(groupFlow.getStudent());
        }
        groupFlows.remove(
            groupFlows.stream()
                .filter(groupFlow1 -> groupFlow1.getStudent().equals(groupFlow.getStudent()))
                .findFirst()
                .get());
        groupFlows.add(groupFlow);
      }
    }
    return users.stream().distinct().collect(toList());
  }

  public List<User> getStudentByGroupIdAndCriteria(
      User.Role role,
      String firstName,
      String lastName,
      String ref,
      PageFromOne page,
      BoundedPageSize pageSize,
      EnableStatus status,
      Sex sex,
      String groupId
  ) {
    return filterUserFromTwoList(
        getByGroupId(groupId),
        getByCriteria(role, firstName, lastName, ref, page, pageSize, status, sex)
    );
  }

  private List<User> filterUserFromTwoList(
      List<User> givenData, List<User> toCompare) {
    return givenData.stream().filter(toCompare::contains).collect(toList());
  }
}
