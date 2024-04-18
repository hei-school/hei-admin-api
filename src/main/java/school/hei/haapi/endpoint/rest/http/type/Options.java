package school.hei.haapi.endpoint.rest.http.type;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Options {
  private final String version = "1.0";
  private final String cacheControl = "no-cache";
  // correlation-id = UUID v4
  private String correlationId;
  // account-identifier = msisdn;tel-num (msisdn;0340343500003)
  private String userAccountIdentifier;
  // language = FR / MG
  private String userLanguage;
  // partner = TestMVola (example)
  private String partnerName;

  public String getCorrelationId() {
    return UUID.randomUUID().toString();
  }
}
