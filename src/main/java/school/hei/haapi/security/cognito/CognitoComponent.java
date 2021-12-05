package school.hei.haapi.security.cognito;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import java.net.MalformedURLException;
import java.text.ParseException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.exception.ApiException;
import school.hei.haapi.exception.ForbiddenException;

@Component
@AllArgsConstructor
public class CognitoComponent {

  private final JwtConfiguration jwtConfiguration;
  private final JwtProcessor jwtProcessor;

  public String findEmailByBearer(String bearer) {
    JWTClaimsSet claims = null;
    try {
      claims = jwtProcessor.configurableJwtProcessor().process(bearer, null);
    } catch (BadJOSEException | ParseException | JOSEException e) {
      throw new ForbiddenException(e.getMessage());
    } catch (MalformedURLException e) {
      throw new ApiException(e.getMessage(), ApiException.ExceptionType.SERVER_EXCEPTION);
    }
    verifyToken(claims);
    return getEmail(claims);
  }

  private String getEmail(JWTClaimsSet claims) {
    return claims.getClaims().get(JwtConfiguration.EMAIL_FIELD).toString();
  }

  private void verifyToken(JWTClaimsSet claims) {
    if (!claims.getIssuer().equals(this.jwtConfiguration.getCognitoUserPoolUrl())) {
      throw new ForbiddenException("Not a valid Token");
    }
  }
}
