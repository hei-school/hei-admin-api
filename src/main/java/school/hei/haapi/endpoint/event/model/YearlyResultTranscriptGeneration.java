package school.hei.haapi.endpoint.event.model;

import java.time.Duration;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.User;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class YearlyResultTranscriptGeneration extends PojaEvent {
  private final String userId;
  private final YearlyResult yearlyResult;
  private final User principal;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(3);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(100);
  }
}
