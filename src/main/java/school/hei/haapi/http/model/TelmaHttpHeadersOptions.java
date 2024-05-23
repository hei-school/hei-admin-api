package school.hei.haapi.http.model;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TelmaHttpHeadersOptions {
  private final String version = "1.0";
  private final String cacheControl = "no-cache";
  // corelation-id = UUID v4()
  private String correlationId;
  // account-identifier = msisdn;tel-num (msisdn;0340343500003)
  // TODO: define it as env var
  private String userAccountIdentifier;
  // language = FR / MG
  private String userLanguage = "FR";
  // partner = TestMVola (example)
  private String partnerName;

  public String getCorrelationId() {
    return UUID.randomUUID().toString();
  }
}
