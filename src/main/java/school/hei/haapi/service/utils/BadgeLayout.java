package school.hei.haapi.service.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import school.hei.haapi.model.Badge;

public final class BadgeLayout {
  public static final int BADGES_PER_ROW = 2;
  public static final int ROWS_PER_PAGE = 5;
  private static final int BADGES_PER_PAGE = BADGES_PER_ROW * ROWS_PER_PAGE;

  private static final List<FontSize> LAST_NAME_FONT_SIZES =
      List.of(
          new FontSize(14, "10pt"),
          new FontSize(16, "8.5pt"),
          new FontSize(18, "8pt"),
          new FontSize(20, "7.5pt"),
          new FontSize(22, "7pt"));
  private static final String SMALLEST_LAST_NAME_FONT_SIZE = "6pt";

  private BadgeLayout() {}

  public static List<List<List<Badge>>> pagesOf(List<Badge> badges) {
    var pages = new ArrayList<List<List<Badge>>>();
    for (var pageStart = 0; pageStart < badges.size(); pageStart += BADGES_PER_PAGE) {
      var pageEnd = Math.min(pageStart + BADGES_PER_PAGE, badges.size());
      var rows = new ArrayList<List<Badge>>();
      for (var rowStart = pageStart; rowStart < pageEnd; rowStart += BADGES_PER_ROW) {
        rows.add(badges.subList(rowStart, Math.min(rowStart + BADGES_PER_ROW, pageEnd)));
      }
      pages.add(rows);
    }
    return pages;
  }

  public static String lastNameFontSize(String lastName) {
    var longestWord =
        Arrays.stream(lastName.split("[\\s-]+")).mapToInt(String::length).max().orElse(0);
    return LAST_NAME_FONT_SIZES.stream()
        .filter(fontSize -> longestWord <= fontSize.maxLetters())
        .map(FontSize::size)
        .findFirst()
        .orElse(SMALLEST_LAST_NAME_FONT_SIZE);
  }

  private record FontSize(int maxLetters, String size) {}
}
