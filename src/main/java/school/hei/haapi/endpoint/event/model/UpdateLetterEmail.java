package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import lombok.*;
import school.hei.haapi.endpoint.rest.model.LetterStatus;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
@Getter
public class UpdateLetterEmail extends PojaEvent {

  @JsonProperty("id")
  private String id;

  @JsonProperty("email")
  private String email;

  @JsonProperty("ref")
  private String ref;

  @JsonProperty("description")
  private String description;

  @JsonProperty("status")
  private LetterStatus status;

  @JsonProperty("reason")
  private String reason;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(60);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(60);
  }
}
