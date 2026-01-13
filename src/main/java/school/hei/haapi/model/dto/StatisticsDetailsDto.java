package school.hei.haapi.model.dto;

import school.hei.haapi.endpoint.rest.model.StatisticsDetails;

public record StatisticsDetailsDto(
    long disabled, long enabled, long suspended, long total) {
  public StatisticsDetails toRestStatisticsDetails() {
    return new StatisticsDetails()
        .disabled(disabled)
        .enabled(enabled)
        .suspended(suspended)
        .total(total);
  }
}
