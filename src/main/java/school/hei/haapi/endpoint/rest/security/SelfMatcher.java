package school.hei.haapi.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class SelfMatcher extends RequestIdMatcher {
  public SelfMatcher(HttpMethod method, String antPattern, String stringBeforeId) {
    super(method, antPattern, stringBeforeId);
  }

  @Override
  public boolean matches(HttpServletRequest request) {
    var antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
    if (!antMatcher.matches(request)) {
      return false;
    }
    var principal = AuthProvider.getPrincipal();
    var userIdFromRequest = getRequestId(request);
    return Objects.equals(userIdFromRequest, principal.getUserId());
  }
}
