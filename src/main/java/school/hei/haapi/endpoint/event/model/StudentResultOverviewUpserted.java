package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor(onConstructor_ = @JsonCreator)
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentResultOverviewUpserted extends PojaEvent {

  @JsonProperty("promotionId")
  private String promotionId;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(10);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(60);
  }
}
