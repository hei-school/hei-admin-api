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
import school.hei.haapi.endpoint.rest.mapper.GroupFlowMapper;
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
          mock(GroupFlowMapper.class),
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
  void
      a_repeat_between_two_groups_each_dedicated_to_only_that_level_keeps_the_most_recent_one_even_with_a_tight_gap() {
    // "K5" belongs to a promotion created a year after "K2"'s, exactly like a group a repeating
    // student gets folded into. Both K2 and K5 are dedicated to L1 only, which is an unambiguous
    // signal (unlike a multi-level group's own calendar disagreeing with the reference one, see
    // the H1/J2 test above): visiting two groups that each exist only to teach L1 can only mean a
    // repeat, so the most recently started one (K5) wins, regardless of the promotion dates.
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
      a_double_repeater_is_reported_using_each_levels_first_attempt_group_without_an_explicit_repeat_signal() {
    // Student who (in the real world) repeated both L1 and L2: G (L1, failed) -> H (L1 redo,
    // passed, then started L2) -> J (L2 redo, passed, then L3). H and J each belong to their own,
    // later-dated promotion, exactly like a normal, non-repeating group switch would look. Since
    // there is no explicit signal distinguishing the two, findStudentLatestGroupFlowPeriodsAtLevel
    // reads every stint against the student's ORIGINAL (earliest, "G") calendar, which assumes one
    // level per academic year with no repeats: G ends up being read as L1, H as L2 (its own second
    // year on that same calendar) - and by the time J starts, two whole extra (repeat) years have
    // elapsed that the calendar didn't budget for, so J's start falls after every level's window on
    // that calendar and it matches NOTHING. This is the accepted, documented limitation of not
    // auto-detecting repeats: a SINGLE repeat (see the H1/J2 test below) is handled gracefully
    // because it never overruns an ongoing period's window, but compounding (double) repeats can
    // still produce a level with no matching group. See the class javadoc for the trade-off.
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
            flow(student, g, JOIN, "2022-11-05T00:00:00Z"),
            flow(student, g, LEAVE, "2023-08-01T00:00:00Z"),
            flow(student, h, JOIN, "2023-11-05T00:00:00Z"),
            flow(student, h, LEAVE, "2025-08-01T00:00:00Z"),
            flow(student, j, JOIN, "2025-11-05T00:00:00Z"));
    when(groupFlowRepository.findByStudentId(student.getId())).thenReturn(flows);

    var l1Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L1);
    assertEquals(1, l1Periods.size());
    assertEquals(g, l1Periods.getFirst().group());

    var l2Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L2);
    assertEquals(1, l2Periods.size());
    assertEquals(h, l2Periods.getFirst().group());

    var l3Periods = subject.findStudentLatestGroupFlowPeriodsAtLevel(student.getId(), L3);
    assertEquals(List.of(), l3Periods, "compounding repeats overrun the original calendar for L3");
  }

  @Test
  void
      a_normal_progression_into_a_group_whose_own_promotion_looks_like_an_earlier_level_is_not_mistaken_for_a_repeat() {
    // Regression test for a real production case: a student did L1 in "H1" (2022-2023), then
    // simply moved on to L2 (then L3) in "J2" - a plain, non-repeating group switch. But J2's own
    // promotion happens to have started a year after H1's, so J2's own calendar computes "L1" for
    // the very window the student is actually doing L2 in. Querying L1 must return only H1, never
    // J2 (J2's own miscalibrated calendar must not override the student's actual, original one).
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
    groupHasAssignmentsAtLevels(j2, L1, L2, L3);

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
    // Regression test for a real production case (student STD24075): "K4" and "N2" each teach
    // ONLY L1 courses for their own respective intake year -- unlike H1/J2 above, they are not
    // persistent, multi-level groups. Visiting two such single-purpose groups for the same level
    // can only mean a genuine repeat (there is no other explanation), so this is the one case
    // where the most recently started group wins, even though its own promotion started later.
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
    // Regression test for a real production case (student STD24164): "K2" is a persistent,
    // multi-level group (L1 and L2 courses) whose own calendar correctly matches L1 for the
    // student's first stint there; "N3" is a group dedicated to L1 only, joined afterwards. Unlike
    // the H1/J2 case, N3 being dedicated to L1 is an explicit, unambiguous repeat signal that must
    // override K2's own (otherwise correctly matching) L1 candidacy, not just other dedicated
    // groups: the student should be reported in N3 only, not both.
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
    // A student staying in the same promotion "K" the whole time: K1 for L1, then K2 for the
    // first semester of L2, then back to K1 for the second semester of L2. Both stints in K1 must
    // stay distinct (never merged into one), and the L2 query must return the K2 stint together
    // with the SECOND K1 stint only -- not the earlier, L1 one.
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
}
