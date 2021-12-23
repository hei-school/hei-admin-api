package school.hei.haapi.endpoint.rest.security;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static school.hei.haapi.endpoint.rest.security.model.Role.MANAGER;
import static school.hei.haapi.endpoint.rest.security.model.Role.TEACHER;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class SecurityConf extends WebSecurityConfigurerAdapter {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  private final AuthProvider authProvider;
  private final HandlerExceptionResolver exceptionResolver;

  public SecurityConf(
      AuthProvider authProvider,
      // InternalToExternalErrorHandler behind
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
    this.authProvider = authProvider;
    this.exceptionResolver = exceptionResolver;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // @formatter:off
    http
        .exceptionHandling()
        .authenticationEntryPoint( // handle authentication errors
            (req, res, e) -> exceptionResolver.resolveException(req, res, null, e))
        .accessDeniedHandler( // handle authorization errors
            (req, res, e) -> exceptionResolver.resolveException(req, res, null, e))

        // authenticate
        .and()
        .authenticationProvider(authProvider)
        .addFilterBefore(
            aBearerFilter(new NegatedRequestMatcher(new AntPathRequestMatcher("/ping"))),
            AnonymousAuthenticationFilter.class)
        .anonymous()

        // authorize
        .and()
        .authorizeRequests()
        .antMatchers("/ping").permitAll()
        .antMatchers("/whoami/**").authenticated()
        .antMatchers(GET, "/students").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .antMatchers(GET, "/students/**").authenticated()
        .antMatchers(PUT, "/students/**").hasAnyRole(MANAGER.getRole())
        .antMatchers(GET, "/teachers").hasAnyRole(MANAGER.getRole())
        .antMatchers(GET, "/teachers/**").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .antMatchers(PUT, "/teachers/**").hasAnyRole(MANAGER.getRole())
        .antMatchers("/managers/**").hasAnyRole(MANAGER.getRole())
        .antMatchers(GET, "/groups/**").authenticated()
        .antMatchers(PUT, "/groups/**").hasAnyRole(MANAGER.getRole())
        .antMatchers("/**").denyAll()

        // disable superfluous protections
        // Eg if all clients are non-browser then no csrf
        // https://docs.spring.io/spring-security/site/docs/3.2.0.CI-SNAPSHOT/reference/html/csrf.html, Sec 13.3
        .and()
        .csrf().disable() // NOSONAR
        .formLogin().disable()
        .logout().disable();
    // formatter:on
  }

  private BearerAuthFilter aBearerFilter(RequestMatcher requestMatcher) throws Exception {
    BearerAuthFilter bearerFilter = new BearerAuthFilter(requestMatcher, AUTHORIZATION_HEADER);
    bearerFilter.setAuthenticationManager(authenticationManager());
    bearerFilter.setAuthenticationSuccessHandler(
        (httpServletRequest, httpServletResponse, authentication) -> {});
    bearerFilter.setAuthenticationFailureHandler(
        (req, res, e) -> exceptionResolver.resolveException(req, res, null, e));
    return bearerFilter;
  }
}
