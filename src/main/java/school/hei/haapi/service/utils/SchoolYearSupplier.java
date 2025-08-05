package school.hei.haapi.service.utils;

import static java.time.Month.DECEMBER;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

@Component
public class SchoolYearSupplier implements Supplier<String> {
  private final List<Month> schoolYearStartMonths = List.of(OCTOBER, NOVEMBER, DECEMBER);

  private String getSchoolYear() {
    LocalDate now = LocalDate.now();
    LocalDate nextYear = now.plusYears(1L);
    LocalDate precedentYear = now.minusYears(1L);

    if (schoolYearStartMonths.contains(now.getMonth())) {
      return now.getYear() + " - " + nextYear.getYear();
    }

    return precedentYear.getYear() + " - " + now.getYear();
  }

  @Override
  public String get() {
    return getSchoolYear();
  }
}
