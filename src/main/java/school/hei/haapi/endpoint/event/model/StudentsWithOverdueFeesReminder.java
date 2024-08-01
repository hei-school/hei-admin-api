package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.List;
import lombok.*;
import school.hei.haapi.model.Fee;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
@Getter
public class StudentsWithOverdueFeesReminder extends PojaEvent {

  @JsonProperty("id")
  private String id;

  @JsonProperty("students")
  private List<StudentWithOverdueFees> students;

  @Builder
  public record StudentWithOverdueFees(String ref, String feeComment) {
    public static StudentWithOverdueFees from(Fee fee) {
      return new StudentWithOverdueFees(fee.getStudent().getRef(), fee.getComment());
    }
  }

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(60);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(60);
  }
}
