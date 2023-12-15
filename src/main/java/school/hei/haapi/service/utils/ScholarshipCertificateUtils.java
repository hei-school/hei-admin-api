package school.hei.haapi.service.utils;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static school.hei.haapi.service.utils.DataFormatterUtils.instantToCommonDate;
import static school.hei.haapi.service.utils.InstantUtils.Instant_now;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;

public class ScholarshipCertificateUtils {
  public static String getStudentIntroductionSentence(User student) {
    return "Ce "
        + instantToCommonDate(Instant_now)
        + " est régulièrement inscrit.e en "
        + getAcademicYear(student)
        + " année d'informatique - tronc commun"
        + ", année scolaire "
        + instantToCommonDate(student.getEntranceDatetime())
        + " l'étudiant.e suivant.e";
  }

  public static String getAcademicYear(User student) {
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC+3"));
    ZonedDateTime entranceDatetime = student.getEntranceDatetime().atZone(ZoneId.of("UTC+3"));
    int year = (int) ChronoUnit.YEARS.between(entranceDatetime, now);
    return switch (year) {
      case 0 -> "Première";
      case 1 -> "Deuxième";
      case 2 -> "Troisième";
      default -> throw new ApiException(SERVER_EXCEPTION, "Invalid year");
    };
  }
}
