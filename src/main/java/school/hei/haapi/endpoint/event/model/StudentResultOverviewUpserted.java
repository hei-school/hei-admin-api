package school.hei.haapi.endpoint.event.model;

import java.time.Duration;

public class StudentResultOverviewUpserted extends PojaEvent {
  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(10);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(60);
  }
}
