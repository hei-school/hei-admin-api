package school.hei.haapi.endpoint.rest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class FeesOnlyFilter extends OncePerRequestFilter {

  private final boolean feesOnly;

  public FeesOnlyFilter() {
    this("true".equalsIgnoreCase(System.getenv("FEES_ONLY")));
  }

  public FeesOnlyFilter(boolean feesOnly) {
    this.feesOnly = feesOnly;
  }

  private static final List<String> ALLOWED_PREFIXES =
      List.of("/fees", "/students", "/whoami", "/ping", "/authentication", "/health", "/mpbs");

  @Override
  public void doFilterInternal(
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
