package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import school.hei.haapi.model.dto.GradeImportDto;

@EqualsAndHashCode()
@Builder
@ToString
@AllArgsConstructor
@Data
public class GradeImportEvent extends PojaEvent {
  @JsonProperty("grades")
  private List<GradeImportDto> grades;

  @JsonProperty private String examId;

  @JsonProperty("coordinator_email")
  private String coordinatorEmail;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(60);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(60);
  }
}
