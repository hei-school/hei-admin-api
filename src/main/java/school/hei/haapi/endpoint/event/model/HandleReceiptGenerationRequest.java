package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import school.hei.haapi.model.dto.PaymentDto;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
@Getter
@Setter
@Builder
public class HandleReceiptGenerationRequest extends PojaEvent {

  @JsonProperty("payments")
  private List<PaymentDto> payments;

  @JsonProperty("notify_email")
  private String notifyEmail;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(10L);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofMinutes(1L);
  }
}
