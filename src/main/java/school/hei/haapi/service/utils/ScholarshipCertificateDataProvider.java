package school.hei.haapi.service.utils;

import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.service.PromotionService;

@Component
@AllArgsConstructor
public class ScholarshipCertificateDataProvider {
  private final SchoolYearSupplier schoolYearSupplier;
  private final PromotionService promotionService;

  public String getAcademicYearSentence(User student) {
    String academicYear =
        findLastStudentPromotion(student).getLevelStringAt(Instant.now()).orElse("Non défini");
    return academicYear + " en Informatique - parcours " + student.getSpecializationFieldString();
  }

  public String getAcademicYearPromotion(User student) {
    return " année scolaire " + schoolYearSupplier.get();
  }

  private Promotion findLastStudentPromotion(User student) {
    return promotionService.getAllStudentPromotions(student.getId()).getLast();
  }
}
