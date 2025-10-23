package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import school.hei.haapi.model.dto.StudentImportDto;

@EqualsAndHashCode
@Builder
@ToString
@AllArgsConstructor
@Data
public class StudentImportEvent extends PojaEvent {
  @JsonProperty("students")
  private List<StudentImportDto> students;

  @JsonProperty("coordinator_email")
  private String coordinatorEmail;

  @JsonProperty("due_datetime")
  private Instant dueDatetime;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(60);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(60);
  }
}
