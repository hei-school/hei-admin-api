package school.hei.haapi.endpoint.rest.security;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;
import school.hei.haapi.model.exception.ForbiddenException;
import school.hei.haapi.service.AwardedCourseService;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static school.hei.haapi.endpoint.rest.security.model.Role.MANAGER;
import static school.hei.haapi.endpoint.rest.security.model.Role.STUDENT;
import static school.hei.haapi.endpoint.rest.security.model.Role.TEACHER;

@Configuration
@Slf4j
public class SecurityConf extends WebSecurityConfigurerAdapter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String STUDENT_COURSE = "/students/*/courses";

    private final AwardedCourseService awardedCourseService;
    private final AuthProvider authProvider;
    private final HandlerExceptionResolver exceptionResolver;

    public SecurityConf(AuthProvider authProvider,
                        // InternalToExternalErrorHandler behind
                        @Qualifier("handlerExceptionResolver")
                        HandlerExceptionResolver exceptionResolver,
                        AwardedCourseService awardedCourseService) {
        this.authProvider = authProvider;
        this.exceptionResolver = exceptionResolver;
        this.awardedCourseService = awardedCourseService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
    http
        .exceptionHandling()
        .authenticationEntryPoint(
            // note(spring-exception)
            // https://stackoverflow.com/questions/59417122/how-to-handle-usernamenotfoundexception-spring-security
            // issues like when a user tries to access a resource
            // without appropriate authentication elements
            (req, res, e) -> exceptionResolver
                .resolveException(req, res, null, forbiddenWithRemoteInfo(req)))
        .accessDeniedHandler(
            // note(spring-exception): issues like when a user not having required roles
            (req, res, e) -> exceptionResolver
                .resolveException(req, res, null, forbiddenWithRemoteInfo(req)))

        // authenticate
        .and()
        .authenticationProvider(authProvider)
        .addFilterBefore(
            bearerFilter(new NegatedRequestMatcher(
                new OrRequestMatcher(
                    new AntPathRequestMatcher("/ping"),
                    new AntPathRequestMatcher("/**", OPTIONS.toString())
                )
            )),
            AnonymousAuthenticationFilter.class)
        .anonymous()

        // authorize
        .and()
        .authorizeRequests()
        .antMatchers("/ping").permitAll()
        .antMatchers(OPTIONS, "/**").permitAll()
        .antMatchers("/whoami").authenticated()
        .antMatchers(GET, "/students").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .requestMatchers(new SelfMatcher(GET, "/students/*/fees/*", "students")).hasAnyRole(STUDENT.getRole())
        .antMatchers(GET, "/students/*/fees/*").hasAnyRole(MANAGER.getRole())
        .requestMatchers(new SelfMatcher(GET, "/students/*/fees", "students")).hasAnyRole(STUDENT.getRole())
        .requestMatchers(new SelfMatcher(GET, "/students/*/fees/*/payments", "students")).hasAnyRole(STUDENT.getRole())
        .antMatchers(GET, "/students/*/fees/*/payments").hasAnyRole(MANAGER.getRole())
        .antMatchers(POST, "/students/*/fees/*/payments").hasAnyRole(MANAGER.getRole())
        .antMatchers(GET, "/students/*/fees").hasAnyRole(MANAGER.getRole())
        .antMatchers(POST, "/students/*/fees").hasAnyRole(MANAGER.getRole())
        .requestMatchers(new SelfMatcher(GET, "/students/*/fees/*/payments", "students")).hasAnyRole(STUDENT.getRole())
        .antMatchers(GET, "/students/*/fees/*/payments").hasAnyRole(MANAGER.getRole())
        .antMatchers(POST, "/students/*/fees/*/payments").hasAnyRole(MANAGER.getRole())
        .requestMatchers(new SelfMatcher(GET, "/students/*", "students")).hasAnyRole(STUDENT.getRole())
        .antMatchers(GET, "/students/*").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .antMatchers(PUT, "/students/**").hasAnyRole(MANAGER.getRole())
        .requestMatchers(new SelfMatcher(GET, "/students/*/grades", "students")).hasAnyRole(STUDENT.getRole())
        .antMatchers(GET, "/students/*/grades").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .antMatchers(GET, "/fees").hasAnyRole(MANAGER.getRole())
        .antMatchers(GET, "/teachers").hasAnyRole(MANAGER.getRole())
        .requestMatchers(new SelfMatcher(GET, "/teachers/*", "teachers")).hasAnyRole(TEACHER.getRole())
        .antMatchers(GET, "/teachers/**").hasAnyRole(MANAGER.getRole())
        .antMatchers(PUT, "/teachers/**").hasAnyRole(MANAGER.getRole())
        .antMatchers("/managers/**").hasAnyRole(MANAGER.getRole())
        .antMatchers(GET, "/groups").authenticated()
        .antMatchers(GET, "/groups/*").authenticated()
        .antMatchers(GET, "/groups/*/awarded_courses").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .antMatchers(GET, "/groups/*/awarded_courses/*").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .requestMatchers(new AwardedCourseOfTeacherMatcher(awardedCourseService, PUT, "/groups/*/awarded_courses/*/exams")).hasAnyRole(TEACHER.getRole())
        .antMatchers(GET, "/groups/*/awarded_courses/*/exams").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .antMatchers(GET, "/groups/*/awarded_courses/*/exams/*").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .antMatchers(GET, "/groups/*/awarded_courses/*/exams/*/grades").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .requestMatchers(new SelfMatcher(GET, "/groups/*/awarded_courses/*/exams/*/students/*/grade", "students")).hasAnyRole(STUDENT.getRole())
        .antMatchers(GET, "/groups/*/awarded_courses/*/exams/*/students/*/grade").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .antMatchers(GET, "/groups/*/students").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .antMatchers(GET, "/groups/**").hasAnyRole(MANAGER.getRole())
        .antMatchers(PUT, "/groups/**").hasAnyRole(MANAGER.getRole())
        .antMatchers(GET, "/courses").authenticated()
        .antMatchers(PUT, "/courses").hasAnyRole(MANAGER.getRole())
        .antMatchers(PUT, "/courses/**").hasAnyRole(MANAGER.getRole())
        .antMatchers(GET, "/courses/*").authenticated()
        .antMatchers(GET, "/courses/*/exams").authenticated()
        .antMatchers(GET, "/courses/*/exams/*").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .antMatchers(GET, "/courses/*/exams/*/details").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .requestMatchers(new SelfMatcher(GET, "/courses/*/exams/*/participants/*", "participants")).hasAnyRole(STUDENT.getRole())
        .antMatchers(GET, "/courses/*/exams/*/participants/*").hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .requestMatchers(new SelfMatcher(GET, STUDENT_COURSE, "students")).hasAnyRole(STUDENT.getRole())
        .antMatchers(GET, STUDENT_COURSE).hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
        .antMatchers(PUT, STUDENT_COURSE).hasAnyRole(MANAGER.getRole())

        .antMatchers("/**").denyAll()

        // disable superfluous protections
        // Eg if all clients are non-browser then no csrf
        // https://docs.spring.io/spring-security/site/docs/3.2.0.CI-SNAPSHOT/reference/html/csrf.html,
        // Sec 13.3
        .and()
        .csrf().disable() // NOSONAR
        .formLogin().disable()
        .logout().disable();
    // formatter:on
  }

  private Exception forbiddenWithRemoteInfo(HttpServletRequest req) {
    log.info(String.format(
        "Access is denied for remote caller: address=%s, host=%s, port=%s",
        req.getRemoteAddr(), req.getRemoteHost(), req.getRemotePort()));
    return new ForbiddenException("Access is denied");
  }

  private BearerAuthFilter bearerFilter(RequestMatcher requestMatcher) throws Exception {
    BearerAuthFilter bearerFilter = new BearerAuthFilter(requestMatcher, AUTHORIZATION_HEADER);
    bearerFilter.setAuthenticationManager(authenticationManager());
    bearerFilter.setAuthenticationSuccessHandler(
        (httpServletRequest, httpServletResponse, authentication) -> {
        });
    bearerFilter.setAuthenticationFailureHandler(
        (req, res, e) ->
            // note(spring-exception)
            // issues like when a user is not found(i.e. UsernameNotFoundException)
            // or other exceptions thrown inside authentication provider.
            // In fact, this handles other authentication exceptions that are
            // not handled by AccessDeniedException and AuthenticationEntryPoint
            exceptionResolver.resolveException(req, res, null, forbiddenWithRemoteInfo(req)));
    return bearerFilter;
  }
}
