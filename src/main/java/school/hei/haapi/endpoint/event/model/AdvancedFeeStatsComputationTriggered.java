package school.hei.haapi.endpoint.event.model;

import static java.time.ZoneOffset.UTC;
import static java.util.Optional.empty;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsCountType;
import school.hei.haapi.service.utils.DateUtils;
import school.hei.haapi.service.utils.DateUtils.TimeRange;

@EqualsAndHashCode
@ToString
@Getter
public class AdvancedFeeStatsComputationTriggered extends PojaEvent {
  private final LocalDateTime end;
  private final Optional<AdvancedFeeStatsCountType> countType;

  @JsonProperty("begin_datetime")
  private LocalDateTime beginDatetime;

  public LocalDateTime getEndDatetime() {
    LocalDate beginDate = beginDatetime.toLocalDate();
    LocalDateTime endOfMonth =
        beginDate.withDayOfMonth(beginDate.lengthOfMonth()).atTime(23, 59, 59);
    return end.isBefore(endOfMonth) ? end : endOfMonth;
  }

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(10);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofMinutes(1);
  }

  public AdvancedFeeStatsComputationTriggered() {
    TimeRange<Instant> currentMonthRange = DateUtils.getDefaultMonthRange(empty(), empty());
    this.beginDatetime = currentMonthRange.from().atOffset(UTC).toLocalDateTime();
    this.end = currentMonthRange.to().atOffset(UTC).toLocalDateTime();
    this.countType = empty();
  }

  public AdvancedFeeStatsComputationTriggered(
      LocalDateTime beginDatetime,
      LocalDateTime end,
      Optional<AdvancedFeeStatsCountType> countType) {
    this.beginDatetime = beginDatetime.toLocalDate().atStartOfDay();
    this.end = end;
    this.countType = countType;
  }
}
