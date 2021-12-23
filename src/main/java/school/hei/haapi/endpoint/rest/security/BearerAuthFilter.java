package school.hei.haapi.endpoint.rest.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Slf4j
public class BearerAuthFilter extends AbstractAuthenticationProcessingFilter {

  private final String authHeader;
  private final ObjectMapper om;

  protected BearerAuthFilter(RequestMatcher requestMatcher, String authHeader, ObjectMapper om) {
    super(requestMatcher);
    this.authHeader = authHeader;
    this.om = om;
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) {
    String bearer = request.getHeader(authHeader);
    Authentication authenticated = getAuthenticationManager().authenticate(
        new UsernamePasswordAuthenticationToken(bearer, bearer));
    log.info("Principal: {}", authenticated.getPrincipal());
    return authenticated;
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authenticated)
      throws IOException, ServletException {
    super.successfulAuthentication(request, response, chain, authenticated);
    chain.doFilter(request, response);
  }

  @Override
  protected void unsuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
      throws IOException, ServletException {
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    response.setStatus(status.value());

    var restException = new school.hei.haapi.endpoint.rest.model.Exception();
    restException.setType(status.toString());
    restException.setMessage(e.getMessage());
    response.getOutputStream().println(om.writeValueAsString(restException));
  }
}
