package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Data
@ToString
public class FeeArchiveRequested extends PojaEvent {
  @JsonProperty("fee_id")
  private String feeId;

  @JsonProperty("student_ref")
  private String studentRef;

  @JsonProperty("student_first_name")
  private String studentFirstName;

  @JsonProperty("student_last_name")
  private String studentLastName;

  @JsonProperty("total_amount")
  private int totalAmount;

  @JsonProperty("due_datetime")
  private Instant dueDatetime;

  @JsonProperty("comment")
  private String comment;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(1);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofMinutes(1);
  }
}
