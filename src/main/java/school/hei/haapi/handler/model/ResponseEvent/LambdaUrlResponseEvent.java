package school.hei.haapi.handler.model.ResponseEvent;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
@SuppressWarnings("all")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LambdaUrlResponseEvent implements Serializable {
  @JsonProperty("statusCode")
  private int statusCode;

  @JsonProperty("headers")
  private Map<String, String> headers;

  @JsonProperty("body")
  private String body;

  @JsonProperty("cookies")
  private List<String> cookies;

  @JsonProperty("isBase64Encoded")
  private boolean isBase64Encoded;

  public LambdaUrlResponseEvent(int statusCode, Map<String, String> headers, String body) {
    this.statusCode = statusCode;
    this.headers = headers;
    this.body = body;
  }
}
