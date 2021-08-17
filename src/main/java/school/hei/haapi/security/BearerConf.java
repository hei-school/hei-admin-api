package school.hei.haapi.security;

import static school.hei.haapi.security.model.Role.MANAGER;
import static school.hei.haapi.security.model.Role.STUDENT;
import static school.hei.haapi.security.model.Role.TEACHER;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@Order(2)
public class BearerConf extends WebSecurityConfigurerAdapter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private final BearerAuthProvider bearerAuthProvider;
  private final AuthenticationFailureHandler failureHandler;

  public BearerConf(
      final BearerAuthProvider bearerAuthProvider,
      @Qualifier("handlerExceptionResolver") // InternalToExternalErrorHandler behind
          HandlerExceptionResolver exceptionResolver) {
    this.bearerAuthProvider = bearerAuthProvider;
    this.failureHandler = (req, res, e) -> exceptionResolver.resolveException(req, res, null, e);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // @formatter:off
    BearerAuthFilter apikeyFilter =
        new BearerAuthFilter(new AntPathRequestMatcher("/**"), AUTHORIZATION_HEADER);
    apikeyFilter.setAuthenticationManager(authenticationManager());
    apikeyFilter.setAuthenticationSuccessHandler(
        (httpServletRequest, httpServletResponse, authentication) -> {});
    apikeyFilter.setAuthenticationFailureHandler(failureHandler);

    http
        // authenticate
        .addFilterBefore(apikeyFilter, AnonymousAuthenticationFilter.class)
        .authenticationProvider(bearerAuthProvider)
        .exceptionHandling()
        // authorize
        .and()
        .authorizeRequests()

        // Let /ping be callable anonymously (see AnonymousConf)
        // However, /health should be callable by monitor system only as it gives system info (cpu,
        // memory...)
        // .antMatchers("/health").hasAnyRole(MONITOR.getRole())

        .antMatchers("/**")
        .hasAnyRole(STUDENT.getRole(), TEACHER.getRole(), MANAGER.getRole())
        // disable superfluous protections
        .and()

        // https://docs.spring.io/spring-security/site/docs/3.2.0.CI-SNAPSHOT/reference/html/csrf.html, Sec 13.3
        .csrf()
        .disable() // NOSONAR(csrf): if all clients are non-browser then no csrf
        .formLogin()
        .disable()
        .logout()
        .disable();
    // formatter:on
  }
}
