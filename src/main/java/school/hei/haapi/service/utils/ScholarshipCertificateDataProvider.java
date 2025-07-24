package school.hei.haapi.service.utils;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.SpecializationField;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.service.PromotionService;

@Component
@AllArgsConstructor
public class ScholarshipCertificateDataProvider {
  private final SchoolYearGetter schoolYearGetter;
  private final PromotionService promotionService;

  public String getAcademicYearSentence(User student) {
    String academicYear = getAcademicYear(findLastStudentPromotion(student), Instant.now());
    return academicYear + " année d'informatique - parcours " + specializationFiledString(student);
  }

  public String getAcademicYear(Promotion promotion, Instant from) {
    return switch (promotion.findLevelAt(from)) {
      case L1 -> "Première";
      case L2 -> "Deuxième";
      case L3 -> "Troisième";
      case M1 -> "Quatrième";
      case M2 -> "Cinquième";
      case null -> "Non defini";
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
      case EL -> "Écosystème Logiciel";
      default -> throw new ApiException(SERVER_EXCEPTION, "Invalid specialization field");
    };
  }

  private Promotion findLastStudentPromotion(User student) {
    // TODO: getLast orderBy creationDatetime
    return promotionService.getAllStudentPromotions(student.getId()).getFirst();
  }
}
