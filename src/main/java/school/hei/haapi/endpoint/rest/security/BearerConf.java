package school.hei.haapi.endpoint.rest.security;

import static school.hei.haapi.endpoint.rest.security.model.Role.MANAGER;
import static school.hei.haapi.endpoint.rest.security.model.Role.STUDENT;
import static school.hei.haapi.endpoint.rest.security.model.Role.TEACHER;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@Order(2)
public class BearerConf extends WebSecurityConfigurerAdapter {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  private final BearerAuthProvider bearerAuthProvider;
  private final HandlerExceptionResolver exceptionResolver;
  private final ObjectMapper om;

  public BearerConf(
      BearerAuthProvider bearerAuthProvider,
      ObjectMapper om,
      // InternalToExternalErrorHandler behind
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
    this.bearerAuthProvider = bearerAuthProvider;
    this.exceptionResolver = exceptionResolver;
    this.om = om;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // @formatter:off
    BearerAuthFilter bearerFilter = new BearerAuthFilter(
        new AntPathRequestMatcher("/**"),
        AUTHORIZATION_HEADER,
        om);
    bearerFilter.setAuthenticationManager(authenticationManager());
    bearerFilter.setAuthenticationSuccessHandler(
        (httpServletRequest, httpServletResponse, authentication) -> {});
    bearerFilter.setAuthenticationFailureHandler(
        (req, res, e) -> exceptionResolver.resolveException(req, res, null, e));

    http
        // authenticate
        .addFilterBefore(bearerFilter, AnonymousAuthenticationFilter.class)
        .authenticationProvider(bearerAuthProvider)
        .exceptionHandling()

        // authorize
        .and()
        .authorizeRequests()
        .antMatchers("/**")
        .hasAnyRole(STUDENT.getRole(), TEACHER.getRole(), MANAGER.getRole())

        // disable superfluous protections
        .and()
        .csrf().disable() // NOSONAR(csrf): if all clients are non-browser then no csrf (TODO)
        .formLogin().disable()
        .logout().disable();
    // formatter:on
  }
}
