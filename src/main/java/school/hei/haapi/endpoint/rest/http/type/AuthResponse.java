package school.hei.haapi.endpoint.rest.http.type;

import lombok.Getter;

@Getter
public class AuthResponse {
  private String access_token;
  private String scope;
  private String token_type;
  private int expires_in;
}
