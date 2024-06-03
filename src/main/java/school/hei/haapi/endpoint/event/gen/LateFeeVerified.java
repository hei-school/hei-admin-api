package school.hei.haapi.endpoint.event.gen;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
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
@Generated("EventBridge")
public class LateFeeVerified implements Serializable {
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

  public record FeeUser(String ref, String lastName, String firstName, String email) {
    public static FeeUser from(User user) {
      return new FeeUser(user.getRef(), user.getLastName(), user.getFirstName(), user.getEmail());
    }
  }
  ;
}
