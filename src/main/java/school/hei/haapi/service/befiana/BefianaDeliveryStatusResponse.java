package school.hei.haapi.service.befiana;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BefianaDeliveryStatusResponse {
  @JsonProperty("callback_data")
  private String callbackData;

  @JsonProperty("delivery_status")
  private String deliveryStatus;

  private String message;
}
