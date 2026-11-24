package school.hei.haapi.service.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.CycleLevel;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.model.promotion.PromotionLevelOutOfRangeException;

class StudentLevelUtilsTest {

  @Test
  void returns_every_level_up_to_the_current_one_for_a_student_still_in_progress() {
    var promotion = mock(Promotion.class);
    when(promotion.getCycleLevel()).thenReturn(CycleLevel.BACHELOR);
    when(promotion.isOverAt(any(Instant.class))).thenReturn(false);
    when(promotion.getLevelAt(any(Instant.class))).thenReturn(L2);
    var student = studentIn(promotion);

    assertEquals(List.of(L1, L2), StudentLevelUtils.levelsUpToCurrentOf(student));
  }

  @Test
  void returns_every_level_of_the_cycle_once_the_promotion_is_over() {
    var promotion = mock(Promotion.class);
    when(promotion.getCycleLevel()).thenReturn(CycleLevel.BACHELOR);
    when(promotion.isOverAt(any(Instant.class))).thenReturn(true);
    when(promotion.getLevelAt(any(Instant.class)))
        .thenThrow(new PromotionLevelOutOfRangeException(3));
    var student = studentIn(promotion);

    assertEquals(List.of(L1, L2, L3), StudentLevelUtils.levelsUpToCurrentOf(student));
  }

  @Test
  void returns_no_level_for_a_promotion_that_has_not_started_yet() {
    var promotion = mock(Promotion.class);
    when(promotion.getCycleLevel()).thenReturn(CycleLevel.BACHELOR);
    when(promotion.isOverAt(any(Instant.class))).thenReturn(false);
    when(promotion.getLevelAt(any(Instant.class)))
        .thenThrow(new PromotionLevelOutOfRangeException(-1));
    var student = studentIn(promotion);

    assertEquals(List.of(), StudentLevelUtils.levelsUpToCurrentOf(student));
  }

  @Test
  void returns_no_level_for_a_student_without_a_current_group() {
    var student = mock(User.class);
    when(student.findCurrentGroup()).thenReturn(Optional.empty());

    assertEquals(List.of(), StudentLevelUtils.levelsUpToCurrentOf(student));
  }

  private static User studentIn(Promotion promotion) {
    var group = Group.builder().id("g1").promotion(promotion).build();
    var student = mock(User.class);
    when(student.findCurrentGroup()).thenReturn(Optional.of(group));
    return student;
  }
}
