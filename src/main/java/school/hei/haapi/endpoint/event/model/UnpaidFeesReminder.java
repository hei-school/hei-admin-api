package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@Builder
@ToString
@AllArgsConstructor
@Data
public class UnpaidFeesReminder extends PojaEvent {
  @JsonProperty("id")
  private String id;

  @JsonProperty("email")
  private String studentEmail;

  @JsonProperty("remainingAmount")
  private Integer remainingAmount;

  @JsonProperty("dueDatetime")
  private Instant dueDatetime;

  @JsonProperty("comment")
  private String comment;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(30);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
