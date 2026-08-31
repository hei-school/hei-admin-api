package school.hei.haapi.service;

import static java.time.Instant.parse;
import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.LEAVE;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.CycleLevel;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.GroupFlowPeriod;
import school.hei.haapi.model.validator.GroupFlowValidator;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;

class GroupFlowServiceTest {
  private final GroupFlowRepository groupFlowRepository = mock();
  private final CourseAssignmentRepository courseAssignmentRepository = mock();
  private final GroupFlowService subject =
      new GroupFlowService(
          groupFlowRepository,
          mock(GroupRepository.class),
          mock(UserRepository.class),
          mock(GroupFlowValidator.class),
          courseAssignmentRepository);

  private static Promotion promotion() {
    return Promotion.builder()
        .id("promo")
        .startDatetime(parse("2023-11-01T00:00:00Z"))
        .cycleLevel(CycleLevel.BACHELOR)
        .build();
  }

  private static Group k2() {
    return Group.builder().id("k2").ref("K2").promotion(promotion()).build();
  }

  private static Group k3() {
    return Group.builder().id("k3").ref("K3").promotion(promotion()).build();
  }

  private static GroupFlow flow(
      User student, Group group, GroupFlow.GroupFlowType type, String at) {
    return GroupFlow.builder()
        .student(student)
        .group(group)
        .groupFlowType(type)
        .flowDatetime(parse(at))
        .build();
  }

  private static CourseAssignment assignmentAtLevel(StudentLevel level) {
    return CourseAssignment.builder().course(Course.builder().studentLevel(level).build()).build();
  }

  private void groupHasAssignmentsAtLevels(Group group, StudentLevel... levels) {
    when(courseAssignmentRepository.findAllByGroupId(group.getId()))
        .thenReturn(List.of(levels).stream().map(GroupFlowServiceTest::assignmentAtLevel).toList());
  }

  @Test
  void returns_every_group_visited_during_the_requested_level_not_an_earlier_one() {
    var student = User.builder().id("student").build();
    var k2 = k2();
    var k3 = k3();
    groupHasAssignmentsAtLevels(k2, L1, L2);
    groupHasAssignmentsAtLevels(k3, L2);

    var flows =
        List.of(
            flow(student, k2, JOIN, "2023-11-05T00:00:00Z"),
            flow(student, k2, LEAVE, "2024-10-01T00:00:00Z"),
            flow(student, k3, JOIN, "2024-11-10T00:00:00Z"),
            flow(student, k3, LEAVE, "2025-02-01T00:00:00Z"),
            flow(student, k2, JOIN, "2025-02-01T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L2);

    assertEquals(2, periods.size());
    assertEquals(
        List.of(parse("2024-11-10T00:00:00Z")),
        periods.stream().filter(p -> p.group().equals(k3)).map(GroupFlowPeriod::start).toList());
    var currentK2Period =
        periods.stream()
            .filter(p -> p.group().equals(k2))
            .findFirst()
            .orElseThrow(() -> new AssertionError("current K2 stint missing from " + periods));
    assertEquals(parse("2025-02-01T00:00:00Z"), currentK2Period.start());
  }

  @Test
  void does_not_merge_an_older_stint_start_with_a_more_recent_leave() {
    var student = User.builder().id("student").build();
    var k2 = k2();
    groupHasAssignmentsAtLevels(k2, L1);

    var flows =
        List.of(
            flow(student, k2, JOIN, "2023-11-05T00:00:00Z"),
            flow(student, k2, LEAVE, "2024-01-05T00:00:00Z"),
            flow(student, k2, JOIN, "2024-05-05T00:00:00Z"),
            flow(student, k2, LEAVE, "2024-07-05T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L1);

    assertEquals(2, periods.size());
    assertEquals(
        List.of(parse("2023-11-05T00:00:00Z"), parse("2024-05-05T00:00:00Z")),
        periods.stream().map(GroupFlowPeriod::start).sorted().toList());
  }

  @Test
  void excludes_a_stint_whose_promotion_calendar_disagrees_with_the_requested_level() {
    var student = User.builder().id("student").build();
    var k2 = k2();
    groupHasAssignmentsAtLevels(k2, L1, L2);

    var flows =
        List.of(
            flow(student, k2, JOIN, "2023-11-05T00:00:00Z"),
            flow(student, k2, LEAVE, "2024-08-01T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L2);

    assertEquals(List.of(), periods);
  }

  @Test
  void excludes_an_earlier_promotion_runs_stint_even_when_its_ref_shares_the_latest_ones_prefix() {
    var student = User.builder().id("student").build();
    var oldPromotion =
        Promotion.builder()
            .id("promo-2022")
            .startDatetime(parse("2022-11-01T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var newPromotion =
        Promotion.builder()
            .id("promo-2023")
            .startDatetime(parse("2023-11-01T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var failedAttemptGroup = Group.builder().id("k2").ref("K2").promotion(oldPromotion).build();
    var repeatAttemptGroup = Group.builder().id("k5").ref("K5").promotion(newPromotion).build();
    groupHasAssignmentsAtLevels(failedAttemptGroup, L1);
    groupHasAssignmentsAtLevels(repeatAttemptGroup, L1);

    var flows =
        List.of(
            flow(student, failedAttemptGroup, JOIN, "2022-11-05T00:00:00Z"),
            flow(student, failedAttemptGroup, LEAVE, "2023-06-01T00:00:00Z"),
            flow(student, repeatAttemptGroup, JOIN, "2023-11-10T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L1);

    assertEquals(1, periods.size());
    assertEquals(repeatAttemptGroup, periods.getFirst().group());
  }

  @Test
  void keeps_stints_from_the_same_promotion_even_when_their_refs_do_not_share_a_prefix() {
    var student = User.builder().id("student").build();
    var promotion = promotion();
    var k2 = Group.builder().id("k2").ref("K2").promotion(promotion).build();
    var h1 = Group.builder().id("h1").ref("H1").promotion(promotion).build();
    groupHasAssignmentsAtLevels(k2, L1);
    groupHasAssignmentsAtLevels(h1, L1);

    var flows =
        List.of(
            flow(student, k2, JOIN, "2023-11-05T00:00:00Z"),
            flow(student, k2, LEAVE, "2024-02-01T00:00:00Z"),
            flow(student, h1, JOIN, "2024-02-01T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L1);

    assertEquals(
        List.of(h1, k2),
        periods.stream().map(GroupFlowPeriod::group).sorted(comparing(Group::getRef)).toList());
  }

  @Test
  void resolves_every_level_for_a_student_who_never_changed_group_across_the_whole_cycle() {
    var student = User.builder().id("student").build();
    var group = k2();
    groupHasAssignmentsAtLevels(group, L1, L2, L3);

    var flows =
        List.of(
            flow(student, group, JOIN, "2023-11-05T00:00:00Z"),
            flow(student, group, LEAVE, "2026-11-01T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    for (var level : List.of(L1, L2, L3)) {
      var periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), level);
      assertEquals(1, periods.size(), "expected a period for " + level);
      assertEquals(group, periods.getFirst().group());
    }
  }

  @Test
  void
      keeps_the_validated_repeat_stint_even_when_its_promotion_calendar_still_expects_an_earlier_level() {
    var student = User.builder().id("student").build();
    var failedAttemptPromotion =
        Promotion.builder()
            .id("promo-2022")
            .startDatetime(parse("2022-11-01T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var repeatGroupPromotion =
        Promotion.builder()
            .id("promo-2024")
            .startDatetime(parse("2024-11-01T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var failedAttemptGroup =
        Group.builder().id("h").ref("H").promotion(failedAttemptPromotion).build();
    var repeatGroup = Group.builder().id("j").ref("J").promotion(repeatGroupPromotion).build();
    groupHasAssignmentsAtLevels(failedAttemptGroup, L2);
    groupHasAssignmentsAtLevels(repeatGroup, L2);

    var flows =
        List.of(
            flow(student, failedAttemptGroup, JOIN, "2023-11-05T00:00:00Z"),
            flow(student, failedAttemptGroup, LEAVE, "2024-08-01T00:00:00Z"),
            flow(student, repeatGroup, JOIN, "2024-11-10T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L2);

    assertEquals(1, periods.size());
    assertEquals(repeatGroup, periods.getFirst().group());
    assertEquals(parse("2024-11-10T00:00:00Z"), periods.getFirst().start());
  }

  @Test
  void keeps_a_stint_at_the_requested_level_when_the_group_has_no_promotion() {
    var student = User.builder().id("student").build();
    var groupWithoutPromotion = Group.builder().id("no-promo").ref("G1").build();
    groupHasAssignmentsAtLevels(groupWithoutPromotion, L1);

    var flows = List.of(flow(student, groupWithoutPromotion, JOIN, "2023-11-05T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L1);

    assertEquals(1, periods.size());
    assertEquals(groupWithoutPromotion, periods.getFirst().group());
  }
}
