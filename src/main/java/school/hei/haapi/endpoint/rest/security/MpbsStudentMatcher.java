package school.hei.haapi.endpoint.rest.security;

import static school.hei.haapi.endpoint.rest.security.AuthProvider.getPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.service.MpbsService;

@AllArgsConstructor
// TODO: refactor this to a superclass implementing RequestMatcher
public class MpbsStudentMatcher implements RequestMatcher {
  private final HttpMethod method;
  private final String antPattern;
  private final String stringBeforeId;
  private final MpbsService mpbsService;

  @Override
  public boolean matches(HttpServletRequest request) {
    AntPathRequestMatcher antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
    if (!antMatcher.matches(request)) {
      return false;
    }
    Principal principal = getPrincipal();
    String mpbsId = getMpbsId(request);
    return mpbsService.findByIdAndStudentId(mpbsId, principal.getUserId()).isPresent();
  }

  private String getMpbsId(HttpServletRequest request) {
    Pattern SELFABLE_URI_PATTERN = Pattern.compile(stringBeforeId + "/(?<id>[^/]+)(/.*)?");
    Matcher uriMatcher = SELFABLE_URI_PATTERN.matcher(request.getRequestURI());
    return uriMatcher.find() ? uriMatcher.group("id") : null;
  }
}
