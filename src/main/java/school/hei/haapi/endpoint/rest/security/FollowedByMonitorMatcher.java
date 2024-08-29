package school.hei.haapi.endpoint.rest.security;

import static school.hei.haapi.endpoint.rest.security.AuthProvider.getPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.model.User;
import school.hei.haapi.service.UserService;

@Slf4j
@AllArgsConstructor
public class FollowedByMonitorMatcher implements RequestMatcher {
  private final HttpMethod method;
  private final String antPattern;
  private final String stringBeforeId;
  private final UserService userService;

  @Override
  public boolean matches(HttpServletRequest request) {
    AntPathRequestMatcher antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
    if (!antMatcher.matches(request)) {
      return false;
    }
    Principal principal = getPrincipal();
    String followedStudentId = getFollowedStudentId(request);
    List<String> monitorsId = toIds(userService.findMonitorsByStudentId(followedStudentId));
    return monitorsId.contains(principal.getUserId());
  }

  private String getFollowedStudentId(HttpServletRequest request) {
    Pattern SELFABLE_URI_PATTERN = Pattern.compile(stringBeforeId + "/(?<id>[^/]+)(/.*)?");
    Matcher uriMatcher = SELFABLE_URI_PATTERN.matcher(request.getRequestURI());
    return uriMatcher.find() ? uriMatcher.group("id") : null;
  }

  private List<String> toIds(List<User> monitors) {
    return monitors.stream().map(User::getId).toList();
  }
}
