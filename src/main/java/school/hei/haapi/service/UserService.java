package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.TypedUserUpserted;
import school.hei.haapi.endpoint.event.model.gen.UserUpserted;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.validator.UserValidator;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.UserManagerDao;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EventProducer eventProducer;
    private final UserValidator userValidator;
    private final UserManagerDao userManagerDao;
    private final GroupRepository groupRepository;

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
        eventProducer.accept(users.stream().map(this::toTypedEvent).collect(toUnmodifiableList()));
        return savedUsers;
    }

    private TypedUserUpserted toTypedEvent(User user) {
        return new TypedUserUpserted(new UserUpserted().userId(user.getId()).email(user.getEmail()));
    }

    public List<User> getByRole(User.Role role, PageFromOne page, BoundedPageSize pageSize) {
        return getByCriteria(role, "", "", "", page, pageSize);
    }

    public List<User> getByCriteria(User.Role role, String firstName, String lastName, String ref, PageFromOne page, BoundedPageSize pageSize) {
        Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "ref"));
        return userManagerDao.findByCriteria(role, ref, firstName, lastName, pageable);
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
            } else if (groupFlow.getFlowDatetime().isAfter(groupFlows.stream().filter(groupFlow1 -> groupFlow1.getStudent().equals(groupFlow.getStudent())).findFirst().get().getFlowDatetime())) {
                if (groupFlow.getGroupFlowType() == GroupFlow.group_flow_type.LEAVE) {
                    users.remove(groupFlow.getStudent());
                }
                groupFlows.remove(groupFlows.stream().filter(groupFlow1 -> groupFlow1.getStudent().equals(groupFlow.getStudent())).findFirst().get());
                groupFlows.add(groupFlow);
            }
        }
        return users;
    }

    public List<User> getByLinkedCourse(User.Role role, String courseId, PageFromOne page, BoundedPageSize pageSize) {
        Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "ref"));
        return userManagerDao.findByLinkedCourse(role, courseId, pageable);
    }

}
