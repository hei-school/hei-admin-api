package school.hei.haapi.service.befiana;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BefianaSendBulkResponse {
  private String message;
  private List<String> recipients;

  @JsonProperty("sms_segments_each")
  private Integer smsSegmentsEach;

  @JsonProperty("balance_debited")
  private Integer balanceDebited;

  @JsonProperty("send_at")
  private String sendAt;
}
