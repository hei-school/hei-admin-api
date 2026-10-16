package school.hei.haapi.service.befiana;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BefianaMessageResponse {
  private String id;
  private String to;
  private String status;
  private Integer segments;

  @JsonProperty("send_at")
  private Instant sendAt;

  @JsonProperty("sender_name")
  private String senderName;

  @JsonProperty("delivery_time")
  private Instant deliveryTime;
}
