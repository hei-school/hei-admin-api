package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.processing.Generated;
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
<<<<<<< HEAD:src/main/java/school/hei/haapi/endpoint/event/gen/LateFeeVerified.java
@Generated("EventBridge")
public class LateFeeVerified implements Serializable {
=======
public class LateFeeVerified extends PojaEvent {
>>>>>>> 291ed6d (poja-upgrade: 13.4.0):src/main/java/school/hei/haapi/endpoint/event/model/LateFeeVerified.java
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

<<<<<<< HEAD:src/main/java/school/hei/haapi/endpoint/event/gen/LateFeeVerified.java
  public record FeeUser(String id, String ref, String lastName, String firstName, String email) {
    public static FeeUser from(User user) {
      return new FeeUser(
          user.getId(), user.getRef(), user.getLastName(), user.getFirstName(), user.getEmail());
    }
  }
  ;
=======
  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(60);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(60);
  }
>>>>>>> 291ed6d (poja-upgrade: 13.4.0):src/main/java/school/hei/haapi/endpoint/event/model/LateFeeVerified.java
}
