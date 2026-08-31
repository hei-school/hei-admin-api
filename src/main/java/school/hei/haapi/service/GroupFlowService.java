package school.hei.haapi.service;

import static java.time.Instant.now;
import static java.util.Comparator.comparing;
import static java.util.regex.Pattern.compile;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.LEAVE;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.model.CreateGroupFlow;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.GroupFlowPeriod;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.GroupFlowValidator;
import school.hei.haapi.repository.CourseAssignmentRepository;
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
  private final CourseAssignmentRepository courseAssignmentRepository;
  private static final Pattern GROUP_TRAILING_DIGITS = compile("\\d+$");

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
        .flowDatetime(now())
        .groupFlowType(GroupFlow.GroupFlowType.fromValue(toMap.getMoveType().getValue()))
        .build();
  }

  public List<GroupFlowPeriod> findStudentLatestGroupFlowPeriodsAtLevel(
      String studentId, StudentLevel level) {
    var groupFlows = repository.findByStudentId(studentId);
    var groupFlowsByGroup = groupFlows.stream().collect(Collectors.groupingBy(GroupFlow::getGroup));
    var groupFlowPeriods =
        groupFlowsByGroup.entrySet().stream()
            .flatMap(entry -> toGroupFlowPeriods(entry.getKey(), entry.getValue()).stream())
            .filter(groupFlowPeriod -> isAtLevel(groupFlowPeriod, level))
            .toList();
    return findLatestGroupFlowPeriods(groupFlowPeriods);
  }

  private boolean isAtLevel(GroupFlowPeriod groupFlowPeriod, StudentLevel level) {
    var group = groupFlowPeriod.group();
    var assignedLevels =
        courseAssignmentRepository.findAllByGroupId(group.getId()).stream()
            .map(courseAssignment -> courseAssignment.getCourse().getStudentLevel())
            .collect(Collectors.toSet());
    if (!assignedLevels.contains(level)) {
      return false;
    }
    if (assignedLevels.size() == 1) {
      return true;
    }

    var promotion = group.getPromotion();
    if (promotion == null) {
      return true;
    }
    return promotion.hasLevelDuring(level, groupFlowPeriod.start(), groupFlowPeriod.end());
  }

  private List<GroupFlowPeriod> toGroupFlowPeriods(Group group, List<GroupFlow> groupFlows) {
    var sortedFlows = groupFlows.stream().sorted(comparing(GroupFlow::getFlowDatetime)).toList();
    var periods = new ArrayList<GroupFlowPeriod>();
    Instant currentStart = null;
    for (var groupFlow : sortedFlows) {
      if (JOIN.equals(groupFlow.getGroupFlowType())) {
        currentStart = groupFlow.getFlowDatetime();
      } else if (LEAVE.equals(groupFlow.getGroupFlowType()) && currentStart != null) {
        periods.add(new GroupFlowPeriod(group, currentStart, groupFlow.getFlowDatetime()));
        currentStart = null;
      }
    }
    if (currentStart != null) {
      periods.add(new GroupFlowPeriod(group, currentStart, now()));
    }
    return periods;
  }

  private List<GroupFlowPeriod> findLatestGroupFlowPeriods(List<GroupFlowPeriod> groupFlowPeriods) {
    if (groupFlowPeriods.isEmpty()) {
      return List.of();
    }

    var latestPeriod =
        groupFlowPeriods.stream().max(comparing(GroupFlowPeriod::start)).orElseThrow();

    return groupFlowPeriods.stream()
        .filter(groupFlowPeriod -> belongsToSameCohortRun(groupFlowPeriod, latestPeriod))
        .toList();
  }

  private boolean belongsToSameCohortRun(
      GroupFlowPeriod groupFlowPeriod, GroupFlowPeriod latestPeriod) {
    var promotion = groupFlowPeriod.group().getPromotion();
    var latestPromotion = latestPeriod.group().getPromotion();
    if (promotion != null && latestPromotion != null) {
      return promotion.getId().equals(latestPromotion.getId());
    }
    return extractGroupPromotion(groupFlowPeriod.group().getRef())
        .equals(extractGroupPromotion(latestPeriod.group().getRef()));
  }

  private String extractGroupPromotion(String groupRef) {
    return GROUP_TRAILING_DIGITS.matcher(groupRef).replaceAll("");
  }
}
