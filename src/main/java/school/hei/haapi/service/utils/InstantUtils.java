package school.hei.haapi.service.utils;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

public class InstantUtils {
  public static final ZoneId UTC0 = ZoneId.of("UTC+0");

  public static final ZoneId UTC3 = ZoneId.of("UTC+3");

  public static Instant currentMondayOfTheWeek() {
    return mondayOfTheWeek(LocalDate.now());
  }

  public static Instant mondayOfTheWeek(LocalDate date) {
    return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .atStartOfDay()
        .atZone(UTC0)
        .toInstant();
  }

  public static Instant currentSaturdayOfTheWeek() {
    return saturdayOfTheWeek(LocalDate.now());
  }

  public static Instant saturdayOfTheWeek(LocalDate date) {
    return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
        .atStartOfDay()
        .atZone(UTC0)
        .toInstant();
  }

  public static Instant getYesterday() {
    LocalDate now = LocalDate.now();
    return now.minusDays(1).atStartOfDay(ZoneId.of("UTC")).plusHours(8).toInstant();
  }

  public static Instant getFirstDayOfActualMonth() {
    return LocalDate.now().with(firstDayOfMonth()).atStartOfDay(ZoneId.of("UTC+3")).toInstant();
  }
}
