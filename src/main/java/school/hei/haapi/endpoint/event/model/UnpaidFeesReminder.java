package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import javax.annotation.processing.Generated;
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
@Generated("EventBridge")
public class UnpaidFeesReminder {
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
}
