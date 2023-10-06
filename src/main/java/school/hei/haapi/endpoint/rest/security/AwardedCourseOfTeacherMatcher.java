package school.hei.haapi.endpoint.rest.security;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.service.AwardedCourseService;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public class AwardedCourseOfTeacherMatcher implements RequestMatcher {
  private final AwardedCourseService awardedCourseService;
  private final HttpMethod method;
  private final String antPattern;

  @Override
  public boolean matches(HttpServletRequest request) {
    AntPathRequestMatcher antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
    Principal principal = AuthProvider.getPrincipal();
    String awardedCourseIdFromRequest = getSelfId(request, "awarded_courses");
    String groupIdFromRequest = getSelfId(request, "groups");
    if (!antMatcher.matches(request)) {
      return false;
    }
    return awardedCourseService.checkTeacherOfAwardedCourse(principal.getUserId(), awardedCourseIdFromRequest ,
            groupIdFromRequest);
  }

  private String getSelfId(HttpServletRequest request, String stringBeforeId) {
    Pattern SELFABLE_URI_PATTERN = Pattern.compile(stringBeforeId + "/(?<id>[^/]+)(/.*)?");
    Matcher uriMatcher = SELFABLE_URI_PATTERN.matcher(request.getRequestURI());
    return uriMatcher.find() ? uriMatcher.group("id") : null;
  }
}
