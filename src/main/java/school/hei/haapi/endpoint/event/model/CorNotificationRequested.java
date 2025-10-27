package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Getter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class CorNotificationRequested extends PojaEvent {
  @JsonProperty("cor_id")
  private String corId;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(1);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofMinutes(1);
  }
}
