package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.model.CycleLevel.BACHELOR;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.service.utils.AcademicYear;
import school.hei.haapi.service.utils.SchoolYearSupplier;

class StudentLevelResolverTest {
  private static final AcademicYear YEAR_2026 = AcademicYear.parse("2026 - 2027");
  private PromotionService promotionService;
  private StudentLevelResolver subject;

  @BeforeEach
  void setUp() {
    promotionService = mock(PromotionService.class);
    subject = new StudentLevelResolver(promotionService, new SchoolYearSupplier());
  }

  @Test
  void level_of_the_academic_year() {
    var student = student();
    givenPromotions(student, bachelorPromotion("2025-11-01T00:00:00Z"));

    assertEquals(Optional.of(L1), subject.findLevelOf(student, AcademicYear.parse("2025 - 2026")));
    assertEquals(Optional.of(L2), subject.findLevelOf(student, YEAR_2026));
  }

  @Test
  void level_comes_from_promotions_even_if_last_joined_group_has_none() {
    var student = student();
    var club = Group.builder().id("club_id").build();
    student.setGroupFlows(
        List.of(
            GroupFlow.builder()
                .student(student)
                .group(club)
                .groupFlowType(JOIN)
                .flowDatetime(Instant.parse("2026-09-01T00:00:00Z"))
                .build()));
    givenPromotions(student, bachelorPromotion("2025-11-01T00:00:00Z"));

    assertEquals(Optional.of(L2), subject.findLevelOf(student, YEAR_2026));
  }

  @Test
  void repeating_student_gets_level_of_its_last_promotion() {
    var student = student();
    givenPromotions(
        student,
        bachelorPromotion("2024-11-01T00:00:00Z"),
        bachelorPromotion("2025-11-01T00:00:00Z"));

    assertEquals(Optional.of(L2), subject.findLevelOf(student, YEAR_2026));
  }

  @Test
  void no_level_without_promotion_for_the_year() {
    var student = student();
    givenPromotions(student, bachelorPromotion("2020-11-01T00:00:00Z"));

    assertEquals(Optional.empty(), subject.findLevelOf(student, YEAR_2026));
  }

  @Test
  void student_keeps_badge_from_the_third_year_of_licence() {
    var l3 = student();
    givenPromotions(l3, bachelorPromotion("2024-11-01T00:00:00Z"));
    assertTrue(subject.keepsBadgeAfter(l3, YEAR_2026));

    var graduated = student();
    givenPromotions(graduated, bachelorPromotion("2020-11-01T00:00:00Z"));
    assertTrue(subject.keepsBadgeAfter(graduated, YEAR_2026));
  }

  @Test
  void student_before_the_third_year_or_without_promotion_gets_a_yearly_badge() {
    var l2 = student();
    givenPromotions(l2, bachelorPromotion("2025-11-01T00:00:00Z"));
    assertFalse(subject.keepsBadgeAfter(l2, YEAR_2026));

    var withoutPromotion = student();
    givenPromotions(withoutPromotion);
    assertFalse(subject.keepsBadgeAfter(withoutPromotion, YEAR_2026));
  }

  private void givenPromotions(User student, Promotion... promotions) {
    when(promotionService.getAllStudentPromotions(student.getId()))
        .thenReturn(new LinkedHashSet<>(List.of(promotions)));
  }

  private static Promotion bachelorPromotion(String startDatetime) {
    return Promotion.builder()
        .id("promotion_" + startDatetime)
        .ref("PROM_" + startDatetime.substring(0, 4))
        .startDatetime(Instant.parse(startDatetime))
        .cycleLevel(BACHELOR)
        .build();
  }

  private static User student() {
    var student = new User();
    student.setId("student1_id");
    student.setRef("STD25001");
    return student;
  }
}
