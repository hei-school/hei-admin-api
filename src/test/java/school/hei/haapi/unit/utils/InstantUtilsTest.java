package school.hei.haapi.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.service.utils.InstantUtils;

@Testcontainers
class InstantUtilsTest {
  @Test
  void get_correct_current_monday_of_the_week() {
    var wednesday8January2020 = LocalDate.of(2020, 1, 8);
    var monday6January2020 = Instant.parse("2020-01-06T00:00:00Z");
    var currentMondayOfTheWeek = InstantUtils.mondayOfTheWeek(wednesday8January2020);
    assertEquals(monday6January2020, currentMondayOfTheWeek);
  }

  @Test
  void get_correct_current_saturday_of_the_week() {
    var wednesday8January2020 = LocalDate.of(2020, 1, 8);
    var saturday11January2020 = Instant.parse("2020-01-11T00:00:00Z");
    var currentMondayOfTheWeek = InstantUtils.saturdayOfTheWeekOrNext(wednesday8January2020);
    assertEquals(saturday11January2020, currentMondayOfTheWeek);
  }

  @Test
  void get_correct_current_monday_of_the_actual_week() {
    var currentMondayOfTheWeek = InstantUtils.currentMondayOfTheWeek();
    assertEquals(InstantUtils.mondayOfTheWeek(LocalDate.now()), currentMondayOfTheWeek);
  }

  @Test
  void get_correct_current_saturday_of_the_actual_week() {
    var currentMondayOfTheWeek = InstantUtils.currentSaturdayOfTheWeekOrNext();
    assertEquals(InstantUtils.saturdayOfTheWeekOrNext(LocalDate.now()), currentMondayOfTheWeek);
  }
}
