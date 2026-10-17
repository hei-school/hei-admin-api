package school.hei.haapi.service.befiana;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BefianaBatchResponse {
  private String id;

  @JsonProperty("recipient_count")
  private Integer recipientCount;

  @JsonProperty("credits_reserved")
  private Integer creditsReserved;

  @JsonProperty("send_at")
  private Instant sendAt;

  @JsonProperty("created_at")
  private Instant createdAt;

  private BefianaStatusCounts counts;
}
