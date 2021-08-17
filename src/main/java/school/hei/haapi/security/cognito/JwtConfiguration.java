package school.hei.haapi.security.cognito;

import java.io.Serializable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfiguration implements Serializable {
  private final String userPoolId;
  private final String region;
  public static final String EMAIL_FIELD = "email";

  public JwtConfiguration(
      @Value("${cognito.jwt.userPoolId}") final String userPoolId,
      @Value("${cognito.jwt.region}") final String region) {
    this.userPoolId = userPoolId;
    this.region = region;
  }

  public String getCognitoIdentityPoolUrlFormat() {
    return String.format(
        "https://cognito-idp.%s.amazonaws.com/%s", this.getRegion(), this.getUserPoolId());
  }

  public String getCognitoJwksUrlFormat() {
    return getCognitoIdentityPoolUrlFormat() + "/.well-known/jwks.json";
  }

  public String getUserPoolId() {
    return this.userPoolId;
  }

  public String getRegion() {
    return this.region;
  }
}
