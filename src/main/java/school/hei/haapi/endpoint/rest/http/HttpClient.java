package school.hei.haapi.endpoint.rest.http;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import school.hei.haapi.endpoint.rest.http.type.Options;

@NoArgsConstructor
@Getter
@Builder
@AllArgsConstructor
public class HttpClient {
  private String consumerKey;
  private String consumerSecret;
  private String token;
  private Options headersOptions;

  public void setToken(String token) {
    this.token = "Bearer " + token;
  }
}
