package school.hei.haapi.endpoint.rest.security;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static school.hei.haapi.model.User.Status.SUSPENDED;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@AllArgsConstructor
@Slf4j
public class SuspendedStudentFilter extends OncePerRequestFilter {
  private final RequestMatcher requiresNonSuspendedStudentRequestMatchers;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (requiresNonSuspendedStudentRequestMatchers.matches(request)) {
      if (SUSPENDED.equals(AuthProvider.getPrincipal().getStatus())) {
        response.sendError(SC_FORBIDDEN, "access is denied");
      }
    }

    filterChain.doFilter(request, response);
  }
}
