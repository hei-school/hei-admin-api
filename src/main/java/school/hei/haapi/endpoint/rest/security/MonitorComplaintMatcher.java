package school.hei.haapi.endpoint.rest.security;

import static school.hei.haapi.endpoint.rest.security.AuthProvider.getPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import school.hei.haapi.model.Complaint;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.ComplaintRepository;
import school.hei.haapi.service.MonitoringStudentService;

public class MonitorComplaintMatcher extends RequestIdMatcher {
  private final ComplaintRepository complaintRepository;
  private final MonitoringStudentService monitoringStudentService;

  public MonitorComplaintMatcher(
      HttpMethod method,
      String antPattern,
      String stringBeforeId,
      ComplaintRepository complaintRepository,
      MonitoringStudentService monitoringStudentService) {
    super(method, antPattern, stringBeforeId);
    this.complaintRepository = complaintRepository;
    this.monitoringStudentService = monitoringStudentService;
  }

  @Override
  public boolean matches(HttpServletRequest request) {
    var antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
    if (!antMatcher.matches(request)) {
      return false;
    }
    var principal = getPrincipal();
    if (principal.getUser().getRole() != User.Role.MONITOR) {
      return false;
    }
    return complaintRepository
        .findById(getRequestId(request))
        .map(Complaint::getStudent)
        .map(User::getId)
        .map(
            studentId ->
                monitoringStudentService
                    .getMonitorIdsByStudentId(studentId)
                    .contains(principal.getUserId()))
        .orElse(false);
  }
}
