package school.hei.haapi.handler.model.requestEvent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestContext {
  @JsonProperty("accountId")
  private String accountId;

  @JsonProperty("apiId")
  private String apiId;

  @JsonProperty("authorizer")
  private Authorizer authorizer;

  @JsonProperty("domainName")
  private String domainName;

  @JsonProperty("domainPrefix")
  private String domainPrefix;

  @JsonProperty("http")
  private Http http;

  @JsonProperty("requestId")
  private String requestId;

  @JsonProperty("time")
  private String time;

  @JsonProperty("timeEpoch")
  private long timeEpoch;
}
