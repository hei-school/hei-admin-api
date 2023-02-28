package school.hei.haapi.unit.utils;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import school.hei.haapi.service.utils.DataFormatterUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataFormatterTest {

  @Test
  void instant_formatted_to_common_date() {
    // For instance, we use french format
    String expected = "8 novembre 2021";
    Instant dueDatetime = Instant.parse("2021-11-08T08:25:24.00Z");
    String actual = DataFormatterUtils.instantToCommonDate(dueDatetime);
    assertEquals(expected, actual);
  }

  @Test
  void number_to_words() {
    int amount = 3_000_000;
    String expected = "TROIS MILLIONS";
    String actual = DataFormatterUtils.numberToWords(amount);
    assertEquals(expected, actual);
  }

}
