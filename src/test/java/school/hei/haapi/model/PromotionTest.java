package school.hei.haapi.model;

import static java.time.LocalTime.MIDNIGHT;
import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M2;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class PromotionTest {
  private static LocalDate fixedDate = LocalDate.of(2025, 12, 6);

  @Test
  void correct_level() {
    var instantToBeL1 = pastInstant(6, 4, 10);
    var instantToBeL2 = pastInstant(5, 4);
    var instantToBeL3 = pastInstant(4, 4, 5);
    var instantToBeM1 = pastInstant(3, 9);
    var instantToBeM2 = pastInstant(2, 4, 11);
    var instantOutOfRange = Instant.now();
    var promotionCreated6YearsAgo =
        Promotion.builder()
            .startDatetime(fixedDate.minusYears(6).atTime(MIDNIGHT).toInstant(UTC))
            .build();

    assertEquals(L1, promotionCreated6YearsAgo.findLevelAt(instantToBeL1).get());
    assertEquals(L2, promotionCreated6YearsAgo.findLevelAt(instantToBeL2).get());
    assertEquals(L3, promotionCreated6YearsAgo.findLevelAt(instantToBeL3).get());
    assertEquals(M1, promotionCreated6YearsAgo.findLevelAt(instantToBeM1).get());
    assertEquals(M2, promotionCreated6YearsAgo.findLevelAt(instantToBeM2).get());
    assertTrue(promotionCreated6YearsAgo.findLevelAt(instantOutOfRange).isEmpty());
  }

  private static Instant pastInstant(int yearsToSubtract, int monthsOffset) {
    return pastInstant(yearsToSubtract, monthsOffset, 0);
  }

  private static Instant pastInstant(int yearsToSubtract, int monthsOffset, int daysOffset) {
    return fixedDate
        .minusYears(yearsToSubtract)
        .plusMonths(monthsOffset)
        .plusDays(daysOffset)
        .atTime(MIDNIGHT)
        .toInstant(UTC);
  }

  @Test
  void getPromotionYearString_shouldReturnCorrectYearRanges() {
    Promotion promotion =
        Promotion.builder()
            .startDatetime(
                LocalDate.of(2023, 10, 1).atStartOfDay(ZoneId.systemDefault()).toInstant())
            .build();

    assertEquals("2023 - 2024", promotion.getPromotionYearString(L1));
    assertEquals("2024 - 2025", promotion.getPromotionYearString(L2));
    assertEquals("2025 - 2026", promotion.getPromotionYearString(L3));
    assertEquals("2026 - 2027", promotion.getPromotionYearString(M1));
    assertEquals("2027 - 2028", promotion.getPromotionYearString(M2));
  }
}
