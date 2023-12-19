package school.hei.haapi.service.utils;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import school.hei.haapi.model.CourseSession;

public class InstantUtils {
  public static Instant Instant_now = LocalDateTime.now().atZone(ZoneId.of("UTC+3")).toInstant();

  public static Instant getCurrentMondayOfTheWeek() {
    return LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .atStartOfDay()
        .atZone(ZoneId.of("UTC+3"))
        .toInstant();
  }

  public static Instant getCurrentSaturdayOfTheWeek() {
    return LocalDate.now()
        .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        .atStartOfDay()
        .atZone(ZoneId.of("UTC+3"))
        .toInstant();
  }

  public static Instant courseSessionStudentAttendanceBeforeIntervalPredicate(
      CourseSession toPredicate) {
    return LocalDateTime.ofInstant(toPredicate.getBegin(), ZoneId.of("UTC+3"))
        .plusHours(1)
        .plusMinutes(55)
        .atZone(ZoneId.of("UTC+3"))
        .toInstant();
  }

  public static Instant courseSessionStudentAttendanceAfterIntervalPredicate(
      CourseSession toPredicate) {
    return LocalDateTime.ofInstant(toPredicate.getBegin(), ZoneId.of("UTC+3"))
        .minusHours(1)
        .minusMinutes(30)
        .atZone(ZoneId.of("UTC+3"))
        .toInstant();
  }

  public static Instant getNextDueDateTime(Instant currentInstant, boolean isEndOfMonth, int delay) {
    YearMonth yearMonthAfterDelay = YearMonth.from(LocalDateTime.ofInstant(currentInstant, ZoneOffset.UTC)).plusMonths(delay);
    LocalDate endOfMonth = yearMonthAfterDelay.atEndOfMonth();
    if (isEndOfMonth) {
      return endOfMonth.atStartOfDay().toInstant(ZoneOffset.UTC);
    }
    int currentDayOfMonth = LocalDateTime.ofInstant(currentInstant, ZoneOffset.UTC).getDayOfMonth();
    int endOfMonthAfterDelay = endOfMonth.getDayOfMonth();
    int dueDateDay = Math.min(currentDayOfMonth, endOfMonthAfterDelay);
    LocalDateTime localDueDateTime = LocalDateTime.of(LocalDate.of(yearMonthAfterDelay.getYear(), yearMonthAfterDelay.getMonth(), dueDateDay), LocalTime.of(0,0,0)) ;
    return localDueDateTime.toInstant(ZoneOffset.UTC);
  }
}
