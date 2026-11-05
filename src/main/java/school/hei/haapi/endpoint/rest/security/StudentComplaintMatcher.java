package school.hei.haapi.endpoint.rest.security;

import static school.hei.haapi.endpoint.rest.security.AuthProvider.getPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import school.hei.haapi.repository.ComplaintRepository;

public class StudentComplaintMatcher extends RequestIdMatcher {
  private final ComplaintRepository complaintRepository;

  public StudentComplaintMatcher(
      HttpMethod method,
      String antPattern,
      String stringBeforeId,
      ComplaintRepository complaintRepository) {
    super(method, antPattern, stringBeforeId);
    this.complaintRepository = complaintRepository;
  }

  @Override
  public boolean matches(HttpServletRequest request) {
    var antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
    if (!antMatcher.matches(request)) {
      return false;
    }
    var principal = getPrincipal();
    return complaintRepository
        .findByIdAndStudent_Id(getRequestId(request), principal.getUserId())
        .isPresent();
  }
}
