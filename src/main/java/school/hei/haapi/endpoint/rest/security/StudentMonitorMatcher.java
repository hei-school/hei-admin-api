package school.hei.haapi.endpoint.rest.security;

import static school.hei.haapi.endpoint.rest.security.AuthProvider.getPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import school.hei.haapi.service.MonitoringStudentService;

public class StudentMonitorMatcher extends RequestIdMatcher {
  private final MonitoringStudentService monitoringStudentService;

  public StudentMonitorMatcher(
      HttpMethod method,
      String antPattern,
      String stringBeforeId,
      MonitoringStudentService monitoringStudentService) {
    super(method, antPattern, stringBeforeId);
    this.monitoringStudentService = monitoringStudentService;
  }

  @Override
  public boolean matches(HttpServletRequest request) {
    var antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
    if (!antMatcher.matches(request)) {
      return false;
    }
    var principal = getPrincipal();
    var followedStudentId = getRequestId(request);
    var monitorsId = monitoringStudentService.getMonitorIdsByStudentId(followedStudentId);
    return monitorsId.contains(principal.getUserId());
  }
}
