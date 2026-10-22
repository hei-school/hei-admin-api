package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Same derivation as {@link ComplaintEmailNotificationRequested}: recipient == the complaint's
 * student means the text is the latest status-history comment (covers IN_PROGRESS and CLOSED alike,
 * since that row always matches the current status); any other recipient means an assignment text.
 */
@Data
@Getter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class ComplaintSmsNotificationRequested extends PojaEvent {
  @JsonProperty("complaint_id")
  private String complaintId;

  @JsonProperty("user_id")
  private String userId;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(30);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
