package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import school.hei.haapi.endpoint.rest.model.ZipReceiptsRequest;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
@Getter
public class SendReceiptZipToEmail extends PojaEvent {

  @JsonProperty("startRequest")
  private Instant startRequest;

  @JsonProperty("request")
  private ZipReceiptsRequest request;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(800);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(60);
  }
}
