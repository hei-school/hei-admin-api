package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import school.hei.haapi.model.mpbs.Mpbs;

@AllArgsConstructor
@Builder
@ToString
@Getter
public class PendingMpbsCheckRequested extends PojaEvent {
  @JsonProperty("to_verify")
  private final Mpbs toVerify;

  @JsonProperty("verify_at")
  private final Instant verifyAt;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(30);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
