package school.hei.haapi.endpoint.event.model;

import static school.hei.haapi.endpoint.event.EventStack.EVENT_STACK_1;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import school.hei.haapi.endpoint.event.EventStack;

@Data
@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class DocumensoDocumentGenerationTriggered extends PojaEvent {
  @JsonProperty("studentId")
  private String studentId;

  @JsonProperty("templateName")
  private String templateName;

  @JsonProperty("generatedById")
  private String generatedById;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(5);
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
