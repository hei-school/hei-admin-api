package school.hei.haapi.endpoint.rest.security.cognito;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import java.net.MalformedURLException;
import java.text.ParseException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.NotImplementedException;

@Component
@AllArgsConstructor
public class CognitoComponent {

  private final JwtConfiguration jwtConfiguration;
  private final JwtProcessor jwtProcessor;

  public String getEmailByIdToken(String idToken) {
    JWTClaimsSet claims;
    try {
      claims = jwtProcessor.configurableJwtProcessor().process(idToken, null);
    } catch (ParseException | BadJOSEException | JOSEException e) {
      /* From Javadoc:
         ParseException – If the string couldn't be parsed to a valid JWT.
         BadJOSEException – If the JWT is rejected.
         JOSEException – If an internal processing exception is encountered. */
      return null;
    } catch (MalformedURLException e) {
      throw new ApiException(ApiException.ExceptionType.SERVER_EXCEPTION, e.getMessage());
    }

    return isClaimsSetValid(claims) ? getEmail(claims) : null;
  }

  private boolean isClaimsSetValid(JWTClaimsSet claims) {
    return claims.getIssuer().equals(this.jwtConfiguration.getCognitoUserPoolUrl());
  }

  private String getEmail(JWTClaimsSet claims) {
    return claims.getClaims().get(JwtConfiguration.EMAIL_FIELD).toString();
  }

  public void createUser(String email) {
    throw new NotImplementedException("TODO");
  }
}
