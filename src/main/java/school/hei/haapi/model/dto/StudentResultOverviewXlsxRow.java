package school.hei.haapi.model.dto;

import java.math.BigDecimal;
import school.hei.haapi.model.ResultOverviewStatus;
import school.hei.haapi.model.StudentResultOverview;

public record StudentResultOverviewXlsxRow(
    String ref,
    String firstName,
    String lastName,
    BigDecimal weightedAverage,
    BigDecimal obtainedCredits,
    BigDecimal totalCredits,
    ResultOverviewStatus status,
    String graduationPromotion) {

  public static StudentResultOverviewXlsxRow from(StudentResultOverview overview) {
    return new StudentResultOverviewXlsxRow(
        overview.getStudent().getRef(),
        overview.getStudent().getFirstName(),
        overview.getStudent().getLastName(),
        overview.getWeightedAverage(),
        overview.getObtainedCredits(),
        overview.getTotalCredits(),
        overview.getStatus(),
        overview.getGraduationPromotion().getName());
  }
}
