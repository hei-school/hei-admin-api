package school.hei.haapi.service.utils;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SchoolYearUtils {
  public static String getSchoolYear() {
    LocalDate now = LocalDate.now();
    LocalDate nextYear = now.plusYears(1L);
    LocalDate precedentYear = now.minusYears(1L);

    if (!getNoMonthStartOfSchoolYear().contains(now.getMonth())) {
      return now.getYear() + " - " + nextYear.getYear();
    }

    return precedentYear.getYear() + " - " + now.getYear();
  }

  public static List<Month> getNoMonthStartOfSchoolYear() {
    return List.of(
        Month.JANUARY,
        Month.FEBRUARY,
        Month.MARCH,
        Month.APRIL,
        Month.MAY,
        Month.JUNE,
        Month.AUGUST,
        Month.SEPTEMBER);
  }
}
