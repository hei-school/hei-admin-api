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
import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;
import school.hei.haapi.model.User;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
@Data
public class LateFeeVerified extends PojaEvent {
  private static final long serialVersionUID = 1L;

  @JsonProperty("concerned_student")
  private FeeUser student;

  @JsonProperty("type")
  private FeeTypeEnum type;

  @JsonProperty("remaining_amount")
  private int remainingAmount;

  @JsonProperty("due_datetime")
  private Instant dueDatetime;

  @JsonProperty("comment")
  private String comment;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(60);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(60);
  }

  public record FeeUser(String id, String ref, String lastName, String firstName, String email) {
    public static FeeUser from(User user) {
      return new FeeUser(
          user.getId(), user.getRef(), user.getLastName(), user.getFirstName(), user.getEmail());
    }
  }
  ;
}
