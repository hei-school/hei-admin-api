package school.hei.haapi.endpoint.event.model;

import static java.time.LocalDateTime.now;
import static java.time.LocalTime.MAX;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AdvancedFeeStatsComputationTriggered extends PojaEvent {
  private final LocalDateTime now = now();

  @JsonProperty("from_datetime")
  private LocalDateTime fromDatetime;

  public LocalDateTime getBeginDatetime() {
    return fromDatetime.toLocalDate().atStartOfDay();
  }

  public LocalDateTime getEndDatetime() {
    LocalDateTime endOfDay = now.toLocalDate().atTime(MAX);
    return now.isBefore(endOfDay) ? now : endOfDay;
  }

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(10);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofMinutes(1);
  }
}
