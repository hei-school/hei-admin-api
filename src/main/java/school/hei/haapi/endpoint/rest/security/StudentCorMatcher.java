package school.hei.haapi.endpoint.rest.security;

import static school.hei.haapi.endpoint.rest.security.AuthProvider.getPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import school.hei.haapi.repository.CorRepository;

public class StudentCorMatcher extends RequestIdMatcher {
  private final CorRepository corRepository;

  public StudentCorMatcher(
      HttpMethod method, String antPattern, String stringBeforeId, CorRepository corRepository) {
    super(method, antPattern, stringBeforeId);
    this.corRepository = corRepository;
  }

  @Override
  public boolean matches(HttpServletRequest request) {
    var antMatcher = new AntPathRequestMatcher(antPattern, method.toString());
    if (!antMatcher.matches(request)) {
      return false;
    }
    var principal = getPrincipal();
    return corRepository
        .findByIdAndStudent_Id(getRequestId(request), principal.getUserId())
        .isPresent();
  }
}
