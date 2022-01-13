package school.hei.haapi.endpoint.rest.security.cognito;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import java.text.ParseException;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.exception.ApiException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

@Component
public class CognitoComponent {

  private final CognitoConf cognitoConf;
  private final CognitoIdentityProviderClient cognitoClient;

  public CognitoComponent(
      CognitoConf cognitoConf,
      CognitoIdentityProviderClient cognitoClient) {
    this.cognitoConf = cognitoConf;
    this.cognitoClient = cognitoClient;
  }

  public String getEmailByIdToken(String idToken) {
    JWTClaimsSet claims;
    try {
      claims = cognitoConf.getJwtProcessor().process(idToken, null);
    } catch (ParseException | BadJOSEException | JOSEException e) {
      /* From Javadoc:
         ParseException – If the string couldn't be parsed to a valid JWT.
         BadJOSEException – If the JWT is rejected.
         JOSEException – If an internal processing exception is encountered. */
      return null;
    }

    return isClaimsSetValid(claims) ? getEmail(claims) : null;
  }

  private boolean isClaimsSetValid(JWTClaimsSet claims) {
    return claims.getIssuer().equals(cognitoConf.getUserPoolUrl());
  }

  private String getEmail(JWTClaimsSet claims) {
    return claims.getClaims().get("email").toString();
  }

  public String createUser(String email) {
    AdminCreateUserRequest createRequest = AdminCreateUserRequest.builder()
        .userPoolId(cognitoConf.getUserPoolId())
        .username(email)
        .build();

    AdminCreateUserResponse createResponse = cognitoClient.adminCreateUser(createRequest);
    if (createResponse == null
        || createResponse.user() == null
        || createResponse.user().username().isBlank()) {
      throw new ApiException(SERVER_EXCEPTION, "Cognito response: " + createResponse);
    }
    return  createResponse.user().username();
  }
}
