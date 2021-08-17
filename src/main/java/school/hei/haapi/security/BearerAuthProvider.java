package school.hei.haapi.security;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import school.hei.haapi.exception.ForbiddenException;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.User;
import school.hei.haapi.security.cognito.CognitoComponent;
import school.hei.haapi.security.model.ApiClient;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class BearerAuthProvider extends AbstractUserDetailsAuthenticationProvider {

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
      throw new ForbiddenException("Bearer can't be null");
    }
    try {
      String email = cognitoComponent.findEmailByBearer(bearer);
      User user = userService.findByEmail(email);
      return new ApiClient(user);
    } catch (NotFoundException e) {
      throw new ForbiddenException("Bearer does not correspond to any user");
    } catch (ForbiddenException e) {
      throw new ForbiddenException(e.getMessage());
    }
  }

  private String getBearer(
      UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
    Object tokenObject = usernamePasswordAuthenticationToken.getCredentials();
    if (!(tokenObject instanceof String) || !((String) tokenObject).startsWith(BEARER_PREFIX)) {
      throw new ForbiddenException("Bearer required but missing");
    }
    return ((String) tokenObject).substring(BEARER_PREFIX.length()).trim();
  }
}
