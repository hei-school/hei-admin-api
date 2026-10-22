package school.hei.haapi.service.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.Badge;

class BadgeLayoutTest {
  @Test
  void split_badges_by_pages_of_two_columns_and_five_rows() {
    var badges =
        IntStream.range(0, 23)
            .mapToObj(i -> new Badge("L", "F", "R" + i, "L1", null, "", "9pt"))
            .toList();

    var pages = BadgeLayout.pagesOf(badges);

    assertEquals(3, pages.size());
    assertEquals(5, pages.get(0).size());
    assertEquals(2, pages.get(2).size());
    assertEquals(1, pages.get(2).get(1).size());
    assertEquals("R22", pages.get(2).get(1).get(0).getRef());
  }

  @Test
  void long_last_names_use_smaller_font() {
    assertEquals("9.8pt", BadgeLayout.lastNameFontSize("RAKOTOARIVELO"));
    assertEquals("8.3pt", BadgeLayout.lastNameFontSize("RANDRIANARIVELO"));
    assertEquals("7.8pt", BadgeLayout.lastNameFontSize("ANDRIAMANOHINIAINA"));
    assertEquals("7.3pt", BadgeLayout.lastNameFontSize("ANDRIAMPARANIMAHEFA"));
    assertEquals("6.9pt", BadgeLayout.lastNameFontSize("RANDRIANARIVELONDRAZAF"));
    assertEquals("5.9pt", BadgeLayout.lastNameFontSize("RANDRIANARIVELONDRAZAFINDRAKOTO"));
    assertEquals("9.8pt", BadgeLayout.lastNameFontSize("RAKOTO ANDRIAMANANA"));
  }
}
