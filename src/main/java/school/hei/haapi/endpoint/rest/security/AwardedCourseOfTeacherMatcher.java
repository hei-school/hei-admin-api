package school.hei.haapi.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.service.CourseAssignmentService;

@AllArgsConstructor
public class AwardedCourseOfTeacherMatcher implements RequestMatcher {
  private final CourseAssignmentService courseAssignmentService;
  private final HttpMethod method;
  private final String antPattern;

  @Override
  public boolean matches(HttpServletRequest request) {
    AntPathRequestMatcher antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
    if (!antMatcher.matches(request)) {
      return false;
    }
    Principal principal = AuthProvider.getPrincipal();
    String courseAssignmentIdFromRequest = getCourseAssignmentId(request, "course_assignments");
    return courseAssignmentService.checkTeacherOfCourseAssignment(
        principal.getUserId(), courseAssignmentIdFromRequest);
  }

  /*
   * TODO: Refactor make a superclass for this
   *  Same function in SelfMatcher
   */
  private String getCourseAssignmentId(HttpServletRequest request, String stringBeforeId) {
    Pattern SELFABLE_URI_PATTERN = Pattern.compile(stringBeforeId + "/(?<id>[^/]+)(/.*)?");
    Matcher uriMatcher = SELFABLE_URI_PATTERN.matcher(request.getRequestURI());
    return uriMatcher.find() ? uriMatcher.group("id") : null;
  }
}
