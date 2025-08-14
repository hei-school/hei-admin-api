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

  @Override
  public String get() {
    LocalDate now = LocalDate.now();
    int nextYear = now.getYear() + 1;
    int precedentYear = now.getYear() - 1;

    if (schoolYearStartMonths.contains(now.getMonth())) {
      return now.getYear() + " - " + nextYear;
    }

    return precedentYear + " - " + now.getYear();
  }
}
