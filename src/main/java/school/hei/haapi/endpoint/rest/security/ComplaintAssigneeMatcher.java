package school.hei.haapi.endpoint.rest.security;

import static school.hei.haapi.endpoint.rest.security.AuthProvider.getPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import school.hei.haapi.repository.ComplaintAssigneeRepository;

public class ComplaintAssigneeMatcher extends RequestIdMatcher {
  private final ComplaintAssigneeRepository complaintAssigneeRepository;

  public ComplaintAssigneeMatcher(
      HttpMethod method,
      String antPattern,
      String stringBeforeId,
      ComplaintAssigneeRepository complaintAssigneeRepository) {
    super(method, antPattern, stringBeforeId);
    this.complaintAssigneeRepository = complaintAssigneeRepository;
  }

  @Override
  public boolean matches(HttpServletRequest request) {
    var antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
    if (!antMatcher.matches(request)) {
      return false;
    }
    var principal = getPrincipal();
    return complaintAssigneeRepository.existsByComplaint_IdAndUser_Id(
        getRequestId(request), principal.getUserId());
  }
}
