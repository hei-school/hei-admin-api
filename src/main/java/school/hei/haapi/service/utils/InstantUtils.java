package school.hei.haapi.service.utils;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import school.hei.haapi.model.CourseSession;
import school.hei.haapi.model.User;

public class InstantUtils {
  public static Instant Instant_now = LocalDateTime.now().atZone(ZoneId.of("UTC+3")).toInstant();

  public static String getScholarityYear(User student) {
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC+3"));
    ZonedDateTime entranceDatetime = student.getEntranceDatetime().atZone(ZoneId.of("UTC+3"));
    int year = (int) ChronoUnit.YEARS.between(entranceDatetime, now);
    return switch (year) {
      case 0 -> "Premiére";
      case 1 -> "Deuxiéme";
      case 2 -> "Troisiéme";
      default ->  null;
    };
  }

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
}
