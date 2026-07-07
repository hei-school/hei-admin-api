package school.hei.haapi.endpoint.rest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Restricts the API surface to a minimal set of prefixes when the application is started in
 * "fees only" mode. Intended as a kill-switch for incidents: keep payments and the student
 * read paths reachable while every other feature is short-circuited with HTTP 403.
 *
 * <p>Activation is controlled by the {@code FEES_ONLY} environment variable / property
 * (default {@code false}). When the flag is off this filter is a no-op.
 */
@Component
@Slf4j
public class FeesOnlyFilter extends OncePerRequestFilter {

  @Value("${FEES_ONLY:false}")
  private boolean feesOnly;

  private static final List<String> ALLOWED_PREFIXES =
      List.of("/fees", "/students", "/whoami", "/ping", "/authentication", "/health", "/mpbs");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (!feesOnly) {
      filterChain.doFilter(request, response);
      return;
    }

    String uri = request.getRequestURI();
    boolean allowed = ALLOWED_PREFIXES.stream().anyMatch(uri::startsWith);

    if (!allowed) {
      log.info("FEES_ONLY mode: blocked request to {}", uri);
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType("application/json");
      response.getWriter().write("{\"message\": \"This endpoint is disabled in FEES_ONLY mode\"}");
      return;
    }

    filterChain.doFilter(request, response);
  }
}
