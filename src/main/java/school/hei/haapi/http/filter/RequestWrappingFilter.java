package school.hei.haapi.http.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestWrappingFilter extends OncePerRequestFilter {

  private static final Set<String> SKIP_METHODS = Set.of("OPTIONS", "HEAD");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (SKIP_METHODS.contains(request.getMethod().toUpperCase())) {
      filterChain.doFilter(request, response);
      return;
    }

    ContentCachingRequestWrapper wrapper;
    if (request instanceof ContentCachingRequestWrapper w) {
      wrapper = w;
    } else {
      wrapper = new ContentCachingRequestWrapper(request);
    }
    wrapper.setAttribute("cachedRequestWrapper", wrapper);
    filterChain.doFilter(wrapper, response);
  }
}
