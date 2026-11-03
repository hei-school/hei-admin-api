package school.hei.haapi.model.dto;

import java.math.BigDecimal;

public record YearlyResultRankingXlsxRow(
    int rank,
    String ref,
    BigDecimal weightedAverage,
    BigDecimal obtainedCredits,
    BigDecimal totalCredits,
    String groupe) {}
