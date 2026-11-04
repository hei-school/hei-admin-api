package school.hei.haapi.endpoint.rest.security;

import static school.hei.haapi.endpoint.rest.security.AuthProvider.getPrincipal;
import static school.hei.haapi.model.User.Role.TEACHER;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import school.hei.haapi.repository.ComplaintAssigneeRepository;

/**
 * Matches only when the caller is a TEACHER assigned to the complaint. A manager/admin/staff member
 * can also be an assignee (e.g. the auto-routed manager) but must keep going through the broader
 * role-based rule for that path instead of being funneled into the teacher-only branch.
 */
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
    if (principal.getUser().getRole() != TEACHER) {
      return false;
    }
    return complaintAssigneeRepository.existsByComplaint_IdAndUser_Id(
        getRequestId(request), principal.getUserId());
  }
}
