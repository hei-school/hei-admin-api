package school.hei.haapi.service;

import static java.util.Comparator.comparing;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.LEAVE;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.mapper.GroupFlowMapper;
import school.hei.haapi.endpoint.rest.model.CreateGroupFlow;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.UpdateGroupFlow;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.Promotion;
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
  private final GroupFlowMapper mapper;
  private final CourseAssignmentRepository courseAssignmentRepository;

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

  private GroupFlow findGroupFlowById(String id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("GroupFlow with id." + id + " not found"));
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

  public List<GroupFlow> getByStudentId(String studentId) {
    findUserById(studentId);
    return repository.findByStudentIdOrderByFlowDatetimeDesc(studentId);
  }

  public GroupFlow update(String id, UpdateGroupFlow toUpdate) {
    var groupFlow = findGroupFlowById(id);
    var group = toUpdate.getGroupId() == null ? null : findGroupById(toUpdate.getGroupId());
    var updatedGroupFlow = mapper.toDomain(groupFlow, toUpdate, group);
    logger(updatedGroupFlow);
    return repository.save(updatedGroupFlow);
  }

  private GroupFlow fromCreateGroupFlowsToGroupFlows(CreateGroupFlow toMap) {
    return GroupFlow.builder()
        .student(findUserById(toMap.getStudentId()))
        .group(findGroupById(toMap.getGroupId()))
        .flowDatetime(Instant.now())
        .groupFlowType(GroupFlow.GroupFlowType.fromValue(toMap.getMoveType().getValue()))
        .build();
  }

  public List<GroupFlowPeriod> findStudentLatestGroupFlowPeriodsAtLevel(
      String studentId, StudentLevel level) {
    var groupFlows = repository.findByStudentId(studentId);
    var groupFlowsByGroup = groupFlows.stream().collect(Collectors.groupingBy(GroupFlow::getGroup));
    var allPeriods =
        groupFlowsByGroup.entrySet().stream()
            .flatMap(entry -> toGroupFlowPeriods(entry.getKey(), entry.getValue()).stream())
            .toList();

    var referenceCalendar = findReferenceCalendar(allPeriods);
    var candidatesAtLevel =
        allPeriods.stream()
            .filter(
                groupFlowPeriod -> isCandidateAtLevel(groupFlowPeriod, level, referenceCalendar))
            .toList();
    var groupFlowPeriodsAtLevel = keepOnlyMostRecentCohort(candidatesAtLevel);
    log.info(
        "Student {} group flow periods at level {} : {}",
        studentId,
        level,
        groupFlowPeriodsAtLevel);
    return groupFlowPeriodsAtLevel;
  }

  private boolean isCandidateAtLevel(
      GroupFlowPeriod groupFlowPeriod, StudentLevel level, Optional<ReferenceCalendar> calendar) {
    var assignedLevels = assignedLevelsOf(groupFlowPeriod.group());
    if (!assignedLevels.contains(level)) {
      return false;
    }
    if (assignedLevels.size() == 1) {
      return true;
    }
    return calendar.map(cal -> isAtLevel(groupFlowPeriod, level, cal)).orElse(true);
  }

  private Optional<ReferenceCalendar> findReferenceCalendar(List<GroupFlowPeriod> allPeriods) {
    return allPeriods.stream()
        .min(comparing(GroupFlowPeriod::start))
        .map(GroupFlowPeriod::group)
        .map(Group::getPromotion)
        .map(promotion -> new ReferenceCalendar(promotion, promotion.getEntranceYear()));
  }

  private record ReferenceCalendar(Promotion promotion, int entranceYear) {}

  private Set<StudentLevel> assignedLevelsOf(Group group) {
    return courseAssignmentRepository.findAllByGroupId(group.getId()).stream()
        .map(courseAssignment -> courseAssignment.getCourse().getStudentLevel())
        .collect(Collectors.toSet());
  }

  private boolean isAtLevel(
      GroupFlowPeriod groupFlowPeriod, StudentLevel level, ReferenceCalendar calendar) {
    return calendar
        .promotion()
        .hasLevelDuring(
            level, groupFlowPeriod.start(), groupFlowPeriod.end(), calendar.entranceYear());
  }

  private List<GroupFlowPeriod> keepOnlyMostRecentCohort(List<GroupFlowPeriod> periods) {
    if (periods.isEmpty()) {
      return List.of();
    }
    var mostRecentPeriod = periods.stream().max(comparing(GroupFlowPeriod::start)).orElseThrow();
    return periods.stream()
        .filter(groupFlowPeriod -> sameCohort(groupFlowPeriod, mostRecentPeriod))
        .toList();
  }

  private boolean sameCohort(GroupFlowPeriod groupFlowPeriod, GroupFlowPeriod other) {
    var promotion = groupFlowPeriod.group().getPromotion();
    var otherPromotion = other.group().getPromotion();
    if (promotion != null && otherPromotion != null) {
      return promotion.getId().equals(otherPromotion.getId());
    }
    return groupFlowPeriod.group().getId().equals(other.group().getId());
  }

  private List<GroupFlowPeriod> toGroupFlowPeriods(Group group, List<GroupFlow> groupFlows) {
    var sortedFlows = groupFlows.stream().sorted(comparing(GroupFlow::getFlowDatetime)).toList();
    var periods = new ArrayList<GroupFlowPeriod>();
    Instant currentStart = null;
    for (var groupFlow : sortedFlows) {
      if (JOIN.equals(groupFlow.getGroupFlowType())) {
        if (currentStart == null) {
          currentStart = groupFlow.getFlowDatetime();
        }
      } else if (LEAVE.equals(groupFlow.getGroupFlowType()) && currentStart != null) {
        periods.add(new GroupFlowPeriod(group, currentStart, groupFlow.getFlowDatetime()));
        currentStart = null;
      }
    }
    if (currentStart != null) {
      periods.add(new GroupFlowPeriod(group, currentStart, null));
    }
    return periods;
  }
}
