package school.hei.haapi.endpoint.event.model;

import static school.hei.haapi.endpoint.event.EventStack.EVENT_STACK_1;

import java.time.Duration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import school.hei.haapi.endpoint.event.EventStack;

@Data
@Getter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString(callSuper = true)
public class PendingDocumensoDocumentsCheckTriggered extends PojaEvent {
  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(10);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofMinutes(1);
  }

  @Override
  public EventStack getEventStack() {
    return EVENT_STACK_1;
  }
}
