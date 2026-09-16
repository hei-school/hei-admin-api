package school.hei.haapi.endpoint.event.model;

import java.time.Duration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Getter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString(callSuper = true)
public class SmsScheduledCampaignDispatchTriggered extends PojaEvent {
  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(5);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofMinutes(1);
  }
}
