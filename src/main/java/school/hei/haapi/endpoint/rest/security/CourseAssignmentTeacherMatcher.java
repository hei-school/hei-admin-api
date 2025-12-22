package school.hei.haapi.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import school.hei.haapi.service.CourseAssignmentService;

public class CourseAssignmentTeacherMatcher extends RequestIdMatcher {
  private final CourseAssignmentService courseAssignmentService;

  public CourseAssignmentTeacherMatcher(
      HttpMethod method,
      String antPattern,
      String stringBeforeId,
      CourseAssignmentService courseAssignmentService) {
    super(method, antPattern, stringBeforeId);
    this.courseAssignmentService = courseAssignmentService;
  }

  @Override
  public boolean matches(HttpServletRequest request) {
    var antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
    if (!antMatcher.matches(request)) {
      return false;
    }
    var principal = AuthProvider.getPrincipal();
    var courseAssignmentIdFromRequest = getRequestId(request);
    return courseAssignmentService.checkTeacherOfCourseAssignment(
        principal.getUserId(), courseAssignmentIdFromRequest);
  }
}
