package school.hei.haapi.endpoint.rest.security;

import static school.hei.haapi.model.User.Status.ALUMNI;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;

public class NonAlumniSelfMatcher extends SelfMatcher {
  public NonAlumniSelfMatcher(HttpMethod method, String antPattern, String stringBeforeId) {
    super(method, antPattern, stringBeforeId);
  }

  @Override
  public boolean matches(HttpServletRequest request) {
    if (!super.matches(request)) {
      return false;
    }
    return !ALUMNI.equals(AuthProvider.getPrincipal().getStatus());
  }
}
