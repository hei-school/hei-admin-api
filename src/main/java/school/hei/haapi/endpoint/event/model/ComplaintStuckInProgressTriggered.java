package school.hei.haapi.endpoint.event.model;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Consumed only — never published from this repo. Meant to be put on the event bus by an external
 * scheduled rule (see UpdateFeesStatusToLateTriggered / SendUnpaidFeesReminderTriggered for the
 * same convention).
 */
@EqualsAndHashCode
@Builder
@ToString
@AllArgsConstructor
public class ComplaintStuckInProgressTriggered extends PojaEvent {
  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(2);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofMinutes(1);
  }
}
