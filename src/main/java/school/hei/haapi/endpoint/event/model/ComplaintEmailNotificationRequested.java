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

@Data
@Getter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class ComplaintEmailNotificationRequested extends PojaEvent {
  @JsonProperty("complaint_id")
  private String complaintId;

  @JsonProperty("user_id")
  private String userId;

  // true: this is an "assigned to a responsible" notice to the student, independent of status
  @JsonProperty("notify_assignment")
  private boolean notifyAssignment;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(1);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofMinutes(1);
  }
}
