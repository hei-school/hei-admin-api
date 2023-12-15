package school.hei.haapi.service.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import school.hei.haapi.model.User;

import static school.hei.haapi.service.utils.DataFormatterUtils.instantToCommonDate;
import static school.hei.haapi.service.utils.InstantUtils.Instant_now;

public class ScholarshipCertificateUtils {
  public static String introductionScolarshipCertificate(User student) {
    return "Ce "
        + instantToCommonDate(Instant_now)
        + " est régulièrement inscrit.e en "
        + getScholarityYear(student)
        + " année d'informatique - tronc commun"
        + ", année scolaire "
        + instantToCommonDate(student.getEntranceDatetime())
        + " "
        + "l'étudiant.e suivant.e";
  }

  public static String getScholarityYear(User student) {
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC+3"));
    ZonedDateTime entranceDatetime = student.getEntranceDatetime().atZone(ZoneId.of("UTC+3"));
    int year = (int) ChronoUnit.YEARS.between(entranceDatetime, now);
    return switch (year) {
      case 0 -> "Premiére";
      case 1 -> "Deuxiéme";
      case 2 -> "Troisiéme";
      default -> null;
    };
  }
}
