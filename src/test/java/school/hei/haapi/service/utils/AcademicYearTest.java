package school.hei.haapi.service.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.exception.BadRequestException;

class AcademicYearTest {
  @Test
  void parse_academic_year() {
    assertEquals(2026, AcademicYear.parse("2026 - 2027").startYear());
    assertEquals(2026, AcademicYear.parse(" 2026-2027 ").startYear());
    assertEquals("2026 - 2027", AcademicYear.parse("2026-2027").label());

    assertThrows(BadRequestException.class, () -> AcademicYear.parse(null));
    assertThrows(BadRequestException.class, () -> AcademicYear.parse("2026"));
    assertThrows(BadRequestException.class, () -> AcademicYear.parse("2026 - 2028"));
  }

  @Test
  void level_is_the_one_of_november_of_the_first_year() {
    assertEquals(
        Instant.parse("2026-11-14T21:00:00Z"), AcademicYear.parse("2026 - 2027").levelInstant());
  }

  @Test
  void badge_expires_when_next_academic_year_starts() {
    // November 1st 2027 (start of the next school year), midnight in Antananarivo (UTC+3)
    assertEquals(
        Instant.parse("2027-10-31T21:00:00Z"), AcademicYear.parse("2026 - 2027").badgeExpiration());
  }
}
