package school.hei.haapi.service.befiana;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BefianaSendResponse {
  private String message;
  private String address;
  private String clientCorrelator;
  private String callbackData;
}
