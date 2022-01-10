package school.hei.haapi.endpoint.rest.security.cognito;

import java.io.Serializable;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfiguration implements Serializable {
  public static final String EMAIL_FIELD = "email";
  @Getter
  private final String cognitoUserPoolUrl;

  public JwtConfiguration(@Value("${aws.cognito.userPool.url}") final String cognitoUserPoolUrl) {
    this.cognitoUserPoolUrl = cognitoUserPoolUrl;
  }

  public String getCognitoJwksUrlFormat() {
    return cognitoUserPoolUrl + "/.well-known/jwks.json";
  }
}
