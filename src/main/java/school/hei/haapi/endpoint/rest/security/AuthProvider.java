package school.hei.haapi.endpoint.rest.security;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class AuthProvider extends AbstractUserDetailsAuthenticationProvider {

  private static final String BEARER_PREFIX = "Bearer ";
  private final UserService userService;
  private final CognitoComponent cognitoComponent;

  @Override
  protected void additionalAuthenticationChecks(
      UserDetails userDetails, UsernamePasswordAuthenticationToken token) {
    // nothing
  }

  @Override
  protected UserDetails retrieveUser(
      String username, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
    String bearer = getBearer(usernamePasswordAuthenticationToken);
    if (bearer == null) {
      throw new UsernameNotFoundException("Bad credentials");
    }

    String email = cognitoComponent.getEmailByBearer(bearer);
    if (email == null) {
      throw new UsernameNotFoundException("Bad credentials");
    }

    return new Principal(userService.getByEmail(email), bearer);
  }

  private String getBearer(
      UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
    Object tokenObject = usernamePasswordAuthenticationToken.getCredentials();
    if (!(tokenObject instanceof String) || !((String) tokenObject).startsWith(BEARER_PREFIX)) {
      return null;
    }
    return ((String) tokenObject).substring(BEARER_PREFIX.length()).trim();
  }
}
