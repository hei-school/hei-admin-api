package school.hei.haapi.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.service.utils.InstantUtils.getCurrentMondayOfTheWeek;
import static school.hei.haapi.service.utils.InstantUtils.getCurrentSaturdayOfTheWeek;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class InstantUtilsTest {
  @Test
  void get_correct_current_monday_of_the_week() {
    var wednesday8January2020 = LocalDate.of(2020, 1, 8);
    Instant monday6January2020 = Instant.parse("2020-01-06T00:00:00Z");
    Instant currentMondayOfTheWeek = getCurrentMondayOfTheWeek(wednesday8January2020);
    assertEquals(monday6January2020, currentMondayOfTheWeek);
  }

  @Test
  void get_correct_current_saturday_of_the_week() {
    var wednesday8January2020 = LocalDate.of(2020, 1, 8);
    Instant saturday11January2020 = Instant.parse("2020-01-11T00:00:00Z");
    Instant currentMondayOfTheWeek = getCurrentSaturdayOfTheWeek(wednesday8January2020);
    assertEquals(saturday11January2020, currentMondayOfTheWeek);
  }

  @Test
  void get_correct_current_monday_of_the_actual_week() {
    Instant currentMondayOfTheWeek = getCurrentMondayOfTheWeek();
    assertEquals(getCurrentMondayOfTheWeek(LocalDate.now()), currentMondayOfTheWeek);
  }

  @Test
  void get_correct_current_saturday_of_the_actual_week() {
    Instant currentMondayOfTheWeek = getCurrentSaturdayOfTheWeek();
    assertEquals(getCurrentSaturdayOfTheWeek(LocalDate.now()), currentMondayOfTheWeek);
  }
}
