package school.hei.haapi.endpoint.event.model;

import static school.hei.haapi.endpoint.event.EventStack.EVENT_STACK_1;

import java.io.Serializable;
import java.time.Duration;
import school.hei.haapi.PojaGenerated;
import school.hei.haapi.endpoint.event.EventStack;

@PojaGenerated
public abstract class PojaEvent implements Serializable {
  public abstract Duration maxConsumerDuration();

  private Duration randomConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(maxConsumerBackoffBetweenRetries().toSeconds());
  }

  public abstract Duration maxConsumerBackoffBetweenRetries();

  public final Duration randomVisibilityTimeout() {
    var eventHandlerInitMaxDuration = Duration.ofSeconds(90); // note(init-visibility)
    return Duration.ofSeconds(
        eventHandlerInitMaxDuration.toSeconds()
            + maxConsumerDuration().toSeconds()
            + randomConsumerBackoffBetweenRetries().toSeconds());
  }

  public EventStack getEventStack() {
    return EVENT_STACK_1;
  }
}
