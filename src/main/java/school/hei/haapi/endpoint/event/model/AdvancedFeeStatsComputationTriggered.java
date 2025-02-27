package school.hei.haapi.endpoint.event.model;

import static java.time.LocalDateTime.now;
import static java.time.LocalTime.MAX;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
public class AdvancedFeeStatsComputationTriggered extends PojaEvent {
  private final LocalDateTime now;

  @JsonProperty("begin_datetime")
  private LocalDateTime beginDatetime;

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

  public AdvancedFeeStatsComputationTriggered() {
    this.beginDatetime = now().toLocalDate().atStartOfDay();
    this.now = now();
  }

  public AdvancedFeeStatsComputationTriggered(LocalDateTime beginDatetime, LocalDateTime now) {
    this.beginDatetime = beginDatetime.toLocalDate().atStartOfDay();
    this.now = now;
  }
}
