package school.hei.haapi.service.utils;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.SpecializationField;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;

@Component
@AllArgsConstructor
public class ScholarshipCertificateDataProvider {
  private final SchoolYearGetter schoolYearGetter;

  public String getAcademicYearSentence(User student) {
    String academicYear = getAcademicYear(student);
    return academicYear + " année d'informatique - parcours " + specializationFiledString(student);
  }

  private String getAcademicYear(User student) {
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

  public String getAcademicYearPromotion(User student) {
    return " année scolaire " + schoolYearGetter.getSchoolYear();
  }

  private String specializationFiledString(User student) {
    SpecializationField specializationField = student.getSpecializationField();
    return switch (specializationField) {
      case COMMON_CORE -> "Tronc commun";
      case TN -> "Transformation Numérique";
      case EL -> "Écosystéme Logiciel";
      default -> throw new ApiException(SERVER_EXCEPTION, "Invalid specialization field");
    };
  }
}
