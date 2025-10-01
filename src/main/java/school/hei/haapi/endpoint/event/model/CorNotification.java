package school.hei.haapi.endpoint.event.model;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import school.hei.haapi.endpoint.rest.model.Cor;

@Getter
@ToString
@AllArgsConstructor
public class CorNotification extends PojaEvent {
  private Cor cor;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(1);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofMinutes(1);
  }
}
