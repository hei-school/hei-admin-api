package school.hei.haapi.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import school.hei.haapi.endpoint.rest.security.model.Principal;

@AllArgsConstructor
@Slf4j
public class SelfMatcher implements RequestMatcher {

  private final HttpMethod method;
  private final String antPattern;
  private final String stringBeforeId;

  @Override
  public boolean matches(HttpServletRequest request) {
    AntPathRequestMatcher antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
    if (!antMatcher.matches(request)) {
      return false;
    }
    Principal principal = AuthProvider.getPrincipal();
    String userIdFromRequest = getSelfId(request);
    return Objects.equals(userIdFromRequest, principal.getUserId());
  }

  private String getSelfId(HttpServletRequest request) {
    Pattern SELFABLE_URI_PATTERN = Pattern.compile(stringBeforeId + "/(?<id>[^/]+)(/.*)?");
    Matcher uriMatcher = SELFABLE_URI_PATTERN.matcher(request.getRequestURI());
    return uriMatcher.find() ? uriMatcher.group("id") : null;
  }
}
