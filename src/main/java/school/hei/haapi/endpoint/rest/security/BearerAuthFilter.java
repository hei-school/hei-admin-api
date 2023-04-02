package school.hei.haapi.endpoint.rest.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Slf4j
public class BearerAuthFilter extends AbstractAuthenticationProcessingFilter {

  private final String authHeader;

  protected BearerAuthFilter(RequestMatcher requestMatcher, String authHeader) {
    super(requestMatcher);
    this.authHeader = authHeader;
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) {
    String bearer = request.getHeader(authHeader);
    return getAuthenticationManager()
        .authenticate(new UsernamePasswordAuthenticationToken(bearer, bearer));
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authenticated)
      throws IOException, ServletException {
    super.successfulAuthentication(request, response, chain, authenticated);
    chain.doFilter(request, response);
  }
}
