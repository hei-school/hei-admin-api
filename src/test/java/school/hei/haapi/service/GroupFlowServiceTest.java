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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.GroupFlowMapper;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.CycleLevel;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.GroupFlowPeriod;
import school.hei.haapi.model.validator.GroupFlowValidator;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;

class GroupFlowServiceTest {
  private final GroupFlowRepository groupFlowRepository = mock();
  private final CourseAssignmentRepository courseAssignmentRepository = mock();
  private final GradeRepository gradeRepository = mock();
  private final GroupFlowService subject =
      new GroupFlowService(
          groupFlowRepository,
          mock(GroupRepository.class),
          mock(UserRepository.class),
          mock(GroupFlowValidator.class),
          mock(GroupFlowMapper.class),
          courseAssignmentRepository,
          gradeRepository);

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

  private static CourseAssignment assignmentAtLevel(Group group, StudentLevel level) {
    return CourseAssignment.builder()
        .id(group.getId() + "-" + level)
        .course(Course.builder().studentLevel(level).build())
        .build();
  }

  private void groupHasAssignmentsAtLevels(Group group, StudentLevel... levels) {
    when(courseAssignmentRepository.findAllByGroupId(group.getId()))
        .thenReturn(List.of(levels).stream().map(l -> assignmentAtLevel(group, l)).toList());
  }

  private void groupHasAssignments(Group group, CourseAssignment... assignments) {
    when(courseAssignmentRepository.findAllByGroupId(group.getId()))
        .thenReturn(List.of(assignments));
  }

  private CourseAssignment gradedAssignment(Group group, StudentLevel level, String studentId) {
    var assignment = assignmentAtLevel(group, level);
    var exam =
        Exam.builder()
            .id(assignment.getId() + "-exam")
            .examinationDate(parse("2020-01-01T00:00:00Z"))
            .build();
    assignment.setExams(new ArrayList<>(List.of(exam)));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(assignment.getId()), studentId))
        .thenReturn(List.of(Grade.builder().exam(exam).score(15.).build()));
    return assignment;
  }

  private CourseAssignment ungradedPastAssignment(
      Group group, StudentLevel level, String studentId) {
    var assignment = assignmentAtLevel(group, level);
    var exam =
        Exam.builder()
            .id(assignment.getId() + "-exam")
            .examinationDate(parse("2020-01-01T00:00:00Z"))
            .build();
    assignment.setExams(new ArrayList<>(List.of(exam)));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(assignment.getId()), studentId))
        .thenReturn(List.of());
    return assignment;
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
  void
      a_repeat_between_two_groups_each_dedicated_to_only_that_level_keeps_the_most_recent_one_even_with_a_tight_gap() {
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
  void a_double_repeater_is_reported_using_each_levels_actual_attempt_group() {
    var student = User.builder().id("student").build();
    var promoG =
        Promotion.builder()
            .id("promo-G")
            .startDatetime(parse("2022-11-01T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var promoH =
        Promotion.builder()
            .id("promo-H")
            .startDatetime(parse("2023-11-01T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var promoJ =
        Promotion.builder()
            .id("promo-J")
            .startDatetime(parse("2024-11-01T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var g = Group.builder().id("g").ref("G").promotion(promoG).build();
    var h = Group.builder().id("h").ref("H").promotion(promoH).build();
    var j = Group.builder().id("j").ref("J").promotion(promoJ).build();
    groupHasAssignmentsAtLevels(g, L1);
    groupHasAssignmentsAtLevels(h, L1, L2);
    groupHasAssignmentsAtLevels(j, L2, L3);

    var flows =
        List.of(
            // L1 attempt 1: fails, leaves g before L1's own calendar year is over.
            flow(student, g, JOIN, "2022-11-05T00:00:00Z"),
            flow(student, g, LEAVE, "2023-08-01T00:00:00Z"),
            // L1 retake in h, continuing straight into L2 -- also left without validating L2.
            flow(student, h, JOIN, "2023-11-05T00:00:00Z"),
            flow(student, h, LEAVE, "2025-08-01T00:00:00Z"),
            // Finishes L2 and moves into L3 in j.
            flow(student, j, JOIN, "2025-11-05T00:00:00Z"),
            flow(student, j, LEAVE, "2027-06-01T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var l1Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L1);
    assertEquals(1, l1Periods.size());
    assertEquals(h, l1Periods.getFirst().group(), "L1 must resolve to the retake (h), not g");

    var l2Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L2);
    assertEquals(1, l2Periods.size());
    assertEquals(j, l2Periods.getFirst().group(), "L2 must resolve to j, superseding h's attempt");

    var l3Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L3);
    assertEquals(1, l3Periods.size());
    assertEquals(j, l3Periods.getFirst().group());
  }

  @Test
  void
      a_normal_progression_into_a_group_whose_own_promotion_looks_like_an_earlier_level_is_not_mistaken_for_a_repeat() {
    var student = User.builder().id("student").build();
    var h1Promotion =
        Promotion.builder()
            .id("promo-h1")
            .startDatetime(parse("2022-11-01T08:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var j2Promotion =
        Promotion.builder()
            .id("promo-j2")
            .startDatetime(parse("2023-11-01T08:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var h1 = Group.builder().id("h1").ref("H1").promotion(h1Promotion).build();
    var j2 = Group.builder().id("j2").ref("J2").promotion(j2Promotion).build();
    groupHasAssignmentsAtLevels(h1, L1, L2, L3);
    // j2's own calendar makes its join date look like the start of J2's L1 year, but the student
    // never actually sat any of J2's (already past) L1 exams -- they only ever did L2/L3 there.
    groupHasAssignments(
        j2,
        ungradedPastAssignment(j2, L1, student.getId()),
        assignmentAtLevel(j2, L2),
        assignmentAtLevel(j2, L3));

    var flows =
        List.of(
            flow(student, h1, JOIN, "2022-11-01T08:00:00Z"),
            flow(student, h1, LEAVE, "2023-11-30T08:00:00Z"),
            flow(student, j2, JOIN, "2023-12-01T07:41:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var l1Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L1);
    assertEquals(1, l1Periods.size());
    assertEquals(h1, l1Periods.getFirst().group());
  }

  @Test
  void a_repeat_between_two_groups_dedicated_to_only_that_one_level_keeps_the_most_recent_group() {
    var student = User.builder().id("student").build();
    var k4Promotion =
        Promotion.builder()
            .id("promo-k4")
            .startDatetime(parse("2024-11-05T08:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var n2Promotion =
        Promotion.builder()
            .id("promo-n2")
            .startDatetime(parse("2025-11-06T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var k4 = Group.builder().id("k4").ref("K4").promotion(k4Promotion).build();
    var n2 = Group.builder().id("n2").ref("N2").promotion(n2Promotion).build();
    groupHasAssignmentsAtLevels(k4, L1);
    groupHasAssignmentsAtLevels(n2, L1);

    var flows =
        List.of(
            flow(student, k4, JOIN, "2024-11-11T14:30:32Z"),
            flow(student, k4, LEAVE, "2025-11-12T08:41:13Z"),
            flow(student, n2, JOIN, "2025-11-24T14:19:54Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var l1Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L1);

    assertEquals(1, l1Periods.size());
    assertEquals(n2, l1Periods.getFirst().group());
  }

  @Test
  void
      a_repeat_into_a_dedicated_group_supersedes_an_earlier_multi_level_groups_attempt_at_that_level() {
    var student = User.builder().id("student").build();
    var k2Promotion =
        Promotion.builder()
            .id("promo-k2")
            .startDatetime(parse("2024-11-05T08:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var n3Promotion =
        Promotion.builder()
            .id("promo-n3")
            .startDatetime(parse("2025-11-06T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var k2 = Group.builder().id("k2-multi").ref("K2").promotion(k2Promotion).build();
    var n3 = Group.builder().id("n3").ref("N3").promotion(n3Promotion).build();
    groupHasAssignmentsAtLevels(k2, L1, L2);
    groupHasAssignmentsAtLevels(n3, L1);

    var flows =
        List.of(
            flow(student, k2, JOIN, "2024-11-11T14:30:32Z"),
            flow(student, k2, LEAVE, "2025-09-12T05:23:00Z"),
            flow(student, n3, JOIN, "2025-10-24T12:07:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var l1Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L1);

    assertEquals(1, l1Periods.size());
    assertEquals(n3, l1Periods.getFirst().group());
  }

  @Test
  void
      a_mid_level_group_switch_within_the_same_promotion_produces_two_distinct_periods_for_the_new_level() {
    var student = User.builder().id("student").build();
    var promotion = promotion();
    var k1 = Group.builder().id("k1").ref("K1").promotion(promotion).build();
    var k2 = Group.builder().id("k2").ref("K2").promotion(promotion).build();
    groupHasAssignmentsAtLevels(k1, L1, L2);
    groupHasAssignmentsAtLevels(k2, L1, L2);

    var flows =
        List.of(
            flow(student, k1, JOIN, "2023-11-05T00:00:00Z"),
            flow(student, k1, LEAVE, "2024-09-10T00:00:00Z"),
            flow(student, k2, JOIN, "2024-11-05T00:00:00Z"),
            flow(student, k2, LEAVE, "2025-04-05T00:00:00Z"),
            flow(student, k1, JOIN, "2025-04-05T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var l1Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L1);
    assertEquals(1, l1Periods.size());
    assertEquals(k1, l1Periods.getFirst().group());
    assertEquals(parse("2023-11-05T00:00:00Z"), l1Periods.getFirst().start());

    var l2Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L2);
    assertEquals(2, l2Periods.size());
    var l2Starts = l2Periods.stream().map(GroupFlowPeriod::start).sorted().toList();
    assertEquals(List.of(parse("2024-11-05T00:00:00Z"), parse("2025-04-05T00:00:00Z")), l2Starts);
    assertEquals(
        List.of(k2, k1),
        l2Periods.stream()
            .sorted(comparing(GroupFlowPeriod::start))
            .map(GroupFlowPeriod::group)
            .toList());
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

  @Test
  void a_single_repeat_straight_into_a_multi_level_group_resolves_every_level_to_its_own_attempt() {
    var student = User.builder().id("student").build();
    var promoH =
        Promotion.builder()
            .id("promo-H-single")
            .startDatetime(parse("2023-11-01T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var promoJ =
        Promotion.builder()
            .id("promo-J-single")
            .startDatetime(parse("2024-11-01T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var h1 = Group.builder().id("h1-single").ref("H1").promotion(promoH).build();
    var j2 = Group.builder().id("j2-single").ref("J2").promotion(promoJ).build();
    groupHasAssignmentsAtLevels(h1, L1, L2);
    groupHasAssignmentsAtLevels(j2, L2, L3);

    var flows =
        List.of(
            flow(student, h1, JOIN, "2023-11-05T00:00:00Z"),
            flow(student, h1, LEAVE, "2025-08-01T00:00:00Z"),
            flow(student, j2, JOIN, "2025-11-05T00:00:00Z"),
            flow(student, j2, LEAVE, "2027-06-01T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var l1Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L1);
    assertEquals(1, l1Periods.size());
    assertEquals(h1, l1Periods.getFirst().group());

    var l2Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L2);
    assertEquals(1, l2Periods.size());
    assertEquals(j2, l2Periods.getFirst().group());

    var l3Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L3);
    assertEquals(1, l3Periods.size());
    assertEquals(j2, l3Periods.getFirst().group());
  }

  @Test
  void
      a_mid_level_split_within_the_same_promotion_keeps_both_stints_even_after_an_earlier_level_switch() {
    var student = User.builder().id("student").build();
    var promotion =
        Promotion.builder()
            .id("promo-K")
            .startDatetime(parse("2024-11-01T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var k1 = Group.builder().id("k1-split").ref("K1").promotion(promotion).build();
    var k2 = Group.builder().id("k2-split").ref("K2").promotion(promotion).build();
    groupHasAssignmentsAtLevels(k1, L1, L2);
    groupHasAssignmentsAtLevels(k2, L1, L2);

    var flows =
        List.of(
            flow(student, k1, JOIN, "2024-11-05T00:00:00Z"),
            flow(student, k1, LEAVE, "2025-09-01T00:00:00Z"),
            flow(student, k2, JOIN, "2025-11-05T00:00:00Z"),
            flow(student, k2, LEAVE, "2026-04-05T00:00:00Z"),
            flow(student, k1, JOIN, "2026-04-05T00:00:00Z"),
            flow(student, k1, LEAVE, "2026-09-05T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var l1Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L1);
    assertEquals(1, l1Periods.size());
    assertEquals(k1, l1Periods.getFirst().group());

    var l2Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L2);
    assertEquals(2, l2Periods.size(), "both the K2 and the K1 comeback stints must show for L2");
    assertEquals(
        List.of(k2, k1),
        l2Periods.stream()
            .sorted(comparing(GroupFlowPeriod::start))
            .map(GroupFlowPeriod::group)
            .toList());
  }

  @Test
  void a_student_who_never_switched_group_resolves_every_level_to_that_single_group() {
    var student = User.builder().id("student").build();
    var promotion =
        Promotion.builder()
            .id("promo-J-stable")
            .startDatetime(parse("2023-11-01T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var j2 = Group.builder().id("j2-stable").ref("J2").promotion(promotion).build();
    groupHasAssignmentsAtLevels(j2, L1, L2, L3);

    var flows =
        List.of(
            flow(student, j2, JOIN, "2023-11-05T00:00:00Z"),
            flow(student, j2, LEAVE, "2027-06-01T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    for (var level : List.of(L1, L2, L3)) {
      var periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), level);
      assertEquals(1, periods.size(), "expected a single period for " + level);
      assertEquals(j2, periods.getFirst().group());
    }
  }

  @Test
  void
      a_transfer_into_a_group_whose_l1_exams_the_student_never_took_does_not_steal_l1_from_the_real_group() {
    var student = User.builder().id("student").build();
    var h1Promotion =
        Promotion.builder()
            .id("promo-h1-real")
            .startDatetime(parse("2022-11-01T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var j2Promotion =
        Promotion.builder()
            .id("promo-j2-real")
            .startDatetime(parse("2023-11-01T00:00:00Z"))
            .cycleLevel(CycleLevel.BACHELOR)
            .build();
    var h1 = Group.builder().id("h1-real").ref("H1").promotion(h1Promotion).build();
    var j2 = Group.builder().id("j2-real").ref("J2").promotion(j2Promotion).build();
    groupHasAssignments(h1, gradedAssignment(h1, L1, student.getId()));
    groupHasAssignments(
        j2,
        ungradedPastAssignment(j2, L1, student.getId()),
        gradedAssignment(j2, L2, student.getId()));

    var flows =
        List.of(
            flow(student, h1, JOIN, "2022-10-02T00:00:00Z"),
            flow(student, h1, LEAVE, "2023-10-01T00:00:00Z"),
            flow(student, j2, JOIN, "2024-01-21T07:19:00Z"),
            flow(student, j2, LEAVE, "2027-06-01T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var l1Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L1);
    assertEquals(1, l1Periods.size());
    assertEquals(h1, l1Periods.getFirst().group(), "L1 must stay on H1, not be stolen by J2");

    var l2Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L2);
    assertEquals(1, l2Periods.size());
    assertEquals(j2, l2Periods.getFirst().group());
  }
}
