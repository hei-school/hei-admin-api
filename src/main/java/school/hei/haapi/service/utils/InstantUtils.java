package school.hei.haapi.service.utils;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import school.hei.haapi.model.CourseSession;

public class InstantUtils {
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

  public static Instant courseSessionStudentAttendanceBeforeIntervalPredicate(CourseSession toPredicate) {
    return LocalDateTime.ofInstant(toPredicate.getBegin(), ZoneId.systemDefault())
        .plusHours(1)
        .plusMinutes(55)
        .atZone(ZoneId.of("UTC+3"))
        .toInstant();
  }

  public static Instant courseSessionStudentAttendanceAfterIntervalPredicate(CourseSession toPredicate) {
    return LocalDateTime.ofInstant(toPredicate.getBegin(), ZoneId.systemDefault())
        .minusHours(1)
        .minusMinutes(30)
        .atZone(ZoneId.of("UTC+3"))
        .toInstant();
  }
}