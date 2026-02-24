package school.hei.haapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M2;
import static school.hei.haapi.model.CycleLevel.BACHELOR;
import static school.hei.haapi.model.CycleLevel.MASTER;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class PromotionTest {

  @Test
  void correct_level_bachelor_cycle() {
    var promotionCreationInstant = Instant.parse("2025-11-08T00:00:00Z");
    var instantToBeL1 = Instant.parse("2026-01-01T00:00:00Z");
    var instantToBeL2 = Instant.parse("2027-01-01T00:00:00Z");
    var instantToBeL3 = Instant.parse("2028-01-01T00:00:00Z");
    var instantOutOfRange = Instant.parse("2029-01-01T00:00:00Z");
    var bachelorPromotion =
        Promotion.builder().cycleLevel(BACHELOR).startDatetime(promotionCreationInstant).build();

    assertEquals(L1, bachelorPromotion.findLevelAt(instantToBeL1).get());
    assertEquals(L2, bachelorPromotion.findLevelAt(instantToBeL2).get());
    assertEquals(L3, bachelorPromotion.findLevelAt(instantToBeL3).get());
    assertTrue(bachelorPromotion.findLevelAt(instantOutOfRange).isEmpty());
  }

  @Test
  void correct_level_master_cycle() {
    var promotionCreationInstant = Instant.parse("2025-11-08T00:00:00Z");
    var instantToBeM1 = Instant.parse("2026-01-01T00:00:00Z");
    var instantToBeM2 = Instant.parse("2027-01-01T00:00:00Z");
    var instantOutOfRange = Instant.parse("2028-01-01T00:00:00Z");
    var masterPromotion =
        Promotion.builder().cycleLevel(MASTER).startDatetime(promotionCreationInstant).build();

    assertEquals(M1, masterPromotion.findLevelAt(instantToBeM1).get());
    assertEquals(M2, masterPromotion.findLevelAt(instantToBeM2).get());
    assertTrue(masterPromotion.findLevelAt(instantOutOfRange).isEmpty());
  }

  @Test
  void getPromotionYearString_shouldReturnCorrectYearRanges() {
    Promotion bachelorPromotion =
        Promotion.builder()
            .cycleLevel(BACHELOR)
            .startDatetime(Instant.parse("2023-11-08T00:00:00Z"))
            .build();

    assertEquals("2023 - 2024", bachelorPromotion.getPromotionYearString(L1));
    assertEquals("2024 - 2025", bachelorPromotion.getPromotionYearString(L2));
    assertEquals("2025 - 2026", bachelorPromotion.getPromotionYearString(L3));
    assertThrows(
        IllegalArgumentException.class,
        () -> bachelorPromotion.getPromotionYearString(M1),
        "Level is not part of the cycle level");
  }
}
