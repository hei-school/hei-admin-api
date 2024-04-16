package school.hei.haapi.endpoint.rest.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;
import school.hei.haapi.model.exception.ForbiddenException;
import school.hei.haapi.service.AwardedCourseService;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static school.hei.haapi.endpoint.rest.security.model.Role.MANAGER;
import static school.hei.haapi.endpoint.rest.security.model.Role.STUDENT;
import static school.hei.haapi.endpoint.rest.security.model.Role.TEACHER;

@Configuration
@Slf4j
@EnableWebSecurity
public class SecurityConf {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String STUDENT_COURSE = "/students/*/courses";
  private final AwardedCourseService awardedCourseService;
  private final AuthProvider authProvider;
  private final HandlerExceptionResolver exceptionResolver;

  public SecurityConf(
      AuthProvider authProvider,
      // InternalToExternalErrorHandler behind
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
      AwardedCourseService awardedCourseService) {
    this.authProvider = authProvider;
    this.exceptionResolver = exceptionResolver;
    this.awardedCourseService = awardedCourseService;
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    return new ProviderManager(authProvider);
  }

  @Bean
  public SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
    // @formatter:off
    httpSecurity
        .exceptionHandling(
            exceptionHandlingConfigurer ->
                exceptionHandlingConfigurer
                    .authenticationEntryPoint(
                        // note(spring-exception)
                        // https://stackoverflow.com/questions/59417122/how-to-handle-usernamenotfoundexception-spring-security
                        // issues like when a user tries to access a resource
                        // without appropriate authentication elements
                        (req, res, e) ->
                            exceptionResolver.resolveException(
                                req, res, null, forbiddenWithRemoteInfo(req)))
                    .accessDeniedHandler(
                        // note(spring-exception): issues like when a user not having required roles
                        (req, res, e) ->
                            exceptionResolver.resolveException(
                                req, res, null, forbiddenWithRemoteInfo(req))))

        // authenticate
        .authenticationProvider(authProvider)
        .addFilterBefore(
            bearerFilter(
                new NegatedRequestMatcher(
                    new OrRequestMatcher(
                        new AntPathRequestMatcher("/ping", GET.name()),
                        new AntPathRequestMatcher("/uuid-created", GET.name()),
                        new AntPathRequestMatcher("/health/db", GET.name()),
                        new AntPathRequestMatcher("/health/email", GET.name()),
                        new AntPathRequestMatcher("/health/event", GET.name()),
                        new AntPathRequestMatcher("/health/bucket", GET.name()),
                        new AntPathRequestMatcher("/**", OPTIONS.toString())))),
            AnonymousAuthenticationFilter.class)

        // authorize
        .authorizeHttpRequests(
            request ->
                request
                    .requestMatchers(GET, "/ping")
                    .permitAll()
                    .requestMatchers(GET, "/health/db")
                    .permitAll()
                    .requestMatchers(GET, "/health/email")
                    .permitAll()
                    .requestMatchers(GET, "/health/event")
                    .permitAll()
                    .requestMatchers(GET, "/health/bucket")
                    .permitAll()
                    .requestMatchers(OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(GET, "/whoami")
                    .authenticated()

                        //
                        // Announcements resources
                        //
                        .requestMatchers(GET, "/teachers/announcements")
                        .hasAnyRole(TEACHER.getRole())
                        .requestMatchers(GET, "/students/announcements")
                        .hasAnyRole(STUDENT.getRole())
                        .requestMatchers(GET, "/announcements")
                        .hasAnyRole(MANAGER.getRole())
                        .requestMatchers(POST, "/announcements")
                        .hasAnyRole(MANAGER.getRole(), TEACHER.getRole())
                    //
                    // Student files resources
                    //

                    .requestMatchers(POST, "/school/files/raw")
                    .hasRole(MANAGER.getRole())
                    .requestMatchers(GET, "/school/files")
                    .hasAnyRole(MANAGER.getRole(), TEACHER.getRole(), STUDENT.getRole())
                    .requestMatchers(GET, "/school/files/*")
                    .hasAnyRole(MANAGER.getRole(), STUDENT.getRole(), TEACHER.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/students/*/work_files", "students"))
                    .hasRole(STUDENT.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/students/*/work_files/*", "students"))
                    .hasRole(STUDENT.getRole())
                    .requestMatchers(POST, "/students/*/group_flows")
                    .hasRole(MANAGER.getRole())
                    .requestMatchers(GET, "/students/*/work_files")
                    .hasRole(MANAGER.getRole())
                    .requestMatchers(GET, "/students/*/work_files/*")
                    .hasRole(MANAGER.getRole())
                    .requestMatchers(POST, "/students/*/work_files/raw")
                    .hasRole(MANAGER.getRole())
                    .requestMatchers(POST, "/students/*/files/raw")
                    .hasRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/students/*/files", "students"))
                    .hasRole(STUDENT.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/students/*/files/*", "students"))
                    .hasRole(STUDENT.getRole())
                    .requestMatchers(GET, "/students/*/files")
                    .hasAnyRole(MANAGER.getRole(), TEACHER.getRole())
                    .requestMatchers(GET, "/students/*/files/*")
                    .hasAnyRole(MANAGER.getRole(), TEACHER.getRole())
                    .requestMatchers(POST, "/students/*/picture/raw")
                    .hasRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(POST, "/teachers/*/picture/raw", "teachers"))
                    .hasRole(TEACHER.getRole())
                    .requestMatchers(POST, "/teachers/*/picture/raw")
                    .hasRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(POST, "/managers/*/picture/raw", "managers"))
                    .hasRole(MANAGER.getRole())
                    // STUDENTS
                    .requestMatchers(GET, "/students")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    // STUDENTS
                    //
                    // Fees resources
                    //
                    .requestMatchers(new SelfMatcher(GET, "/students/*/fees/*/mpbs", "students"))
                    .hasRole(STUDENT.getRole())
                    .requestMatchers(new SelfMatcher(PUT, "/students/*/fees/*/mpbs", "students"))
                    .hasRole(STUDENT.getRole())
                    .requestMatchers(GET, "/students/*/fees/*/mpbs")
                    .hasRole(MANAGER.getRole())
                    .requestMatchers(PUT, "/students/*/fees/*/mpbs")
                    .hasRole(MANAGER.getRole())
                    .requestMatchers(
                        new SelfMatcher(GET, "/students/*/fees/*/mpbs/verifications", "students"))
                    .hasRole(STUDENT.getRole())
                    .requestMatchers(GET, "/students/*/fees/*/mpbs/verifications")
                    .hasRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/students/*/fees/*", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(DELETE, "/students/*/fees/*")
                    .hasRole(MANAGER.getRole())
                    .requestMatchers(GET, "/students/*/fees/*")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/students/*/fees", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(
                        new SelfMatcher(GET, "/students/*/fees/*/payments", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(DELETE, "/students/*/fees/*/payments/*")
                    .hasRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/students/*/fees/*", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/students/*/fees/*")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/students/*/fees", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(
                        new SelfMatcher(GET, "/students/*/fees/*/payments", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/students/*/fees/*/mpbs", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/students/*/fees/*/mpbs")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(PUT, "/students/*/fees/*/mpbs", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(
                        new SelfMatcher(GET, "/students/*/fees/*/mpbs/verifications", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/students/*/fees/*/mpbs_verifications")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(GET, "/students/*/fees/*/payments")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(POST, "/students/*/fees/*/payments")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(GET, "/students/*/fees")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(POST, "/students/*/fees")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(PUT, "/students/*/fees")
                    //
                    // Payments resources
                    //
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(
                        new SelfMatcher(GET, "/students/*/fees/*/payments", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/students/*/fees/*/payments")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(POST, "/students/*/fees/*/payments")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/students/*", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/students/*")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(PUT, "/students/**")
                    .hasAnyRole(MANAGER.getRole())
                    //
                    // Grades resources
                    //
                    .requestMatchers(new SelfMatcher(GET, "/students/*/grades", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/students/*/grades")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(GET, "/fees")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(GET, "/teachers")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/teachers/*", "teachers"))
                    .hasAnyRole(TEACHER.getRole())
                    .requestMatchers(
                        new SelfMatcher(GET, "/students/*/fees/*/payments", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/students/*/fees/*/payments")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(POST, "/students/*/fees/*/payments")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/students/*", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/students/*")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    // scholarship security conf
                    .requestMatchers(
                        new SelfMatcher(GET, "/students/*/scholarship_certificate/raw", "students"))
                    .hasRole(STUDENT.getRole())
                    .requestMatchers(GET, "/students/*/scholarship_certificate/raw")
                    .hasRole(MANAGER.getRole())
                    .requestMatchers(PUT, "/students/**")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/students/*/grades", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/students/*/grades")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(GET, "/fees")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(GET, "/fees/templates")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(PUT, "/fees/templates/*")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(GET, "/fees/templates/*")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(GET, "/teachers")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/teachers/*", "teachers"))
                    .hasAnyRole(TEACHER.getRole())
                    .requestMatchers(GET, "/teachers/**")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(PUT, "/teachers/*", "teachers"))
                    .hasRole(TEACHER.getRole())
                    .requestMatchers(PUT, "/teachers/**")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(PUT, "/managers/*", "managers"))
                    .hasRole(MANAGER.getRole())
                    .requestMatchers("/managers/**")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(GET, "/groups")
                    .authenticated()
                    .requestMatchers(GET, "/groups/*")
                    .authenticated()
                    .requestMatchers(GET, "/groups/*/awarded_courses")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(GET, "/groups/*/awarded_courses/*")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(PUT, "/groups/*/awarded_courses")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(
                        new AwardedCourseOfTeacherMatcher(
                            awardedCourseService, PUT, "/groups/*/awarded_courses/*/exams"))
                    .hasAnyRole(TEACHER.getRole())
                    .requestMatchers(GET, "/groups/*/awarded_courses/*/exams")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(GET, "/groups/*/awarded_courses/*/exams/*")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(GET, "/groups/*/awarded_courses/*/exams/*/grades")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(
                        new SelfMatcher(
                            GET,
                            "/groups/*/awarded_courses/*/exams/*/students/*/grade",
                            "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/groups/*/awarded_courses/*/exams/*/students/*/grade")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(GET, "/awarded_courses")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(
                        new AwardedCourseOfTeacherMatcher(
                            awardedCourseService, PUT, "/groups/*" + "/awarded_courses/*/exams"))
                    .hasAnyRole(TEACHER.getRole())
                    .requestMatchers(GET, "/groups/*/awarded_courses/*/exams")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(GET, "/groups/*/awarded_courses/*/exams/*")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(GET, "/groups/*/awarded_courses/*/exams/*/grades")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(
                        new SelfMatcher(
                            GET,
                            "/groups/*/awarded_courses/*" + "/exams/*/students/*/grade",
                            "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/groups/*/awarded_courses/*/exams/*/students/*/grade")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(GET, "/groups/*/students")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(GET, "/groups/**")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(PUT, "/groups/**")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(GET, "/courses")
                    .authenticated()
                    .requestMatchers(PUT, "/courses")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(PUT, "/courses/**")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(GET, "/courses/*")
                    .authenticated()
                    .requestMatchers(GET, "/courses/*/exams")
                    .authenticated()
                    .requestMatchers(GET, "/courses/*/exams/*")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(GET, "/courses/*/exams/*/details")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(
                        new SelfMatcher(GET, "/courses/*/exams/*/participants/*", "participants"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/courses/*/exams/*/participants/*")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(GET, STUDENT_COURSE, "students"))
                    .hasAnyRole(STUDENT.getRole())
                    //
                    // Comments resources
                    //
                    .requestMatchers(GET, "/comments")
                    .hasAnyRole(MANAGER.getRole(), TEACHER.getRole())
                    .requestMatchers(new SelfMatcher(GET, "/students/*/comments", "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/students/*/comments")
                    .hasAnyRole(MANAGER.getRole(), TEACHER.getRole())
                    .requestMatchers(POST, "/students/*/comments")
                    .hasAnyRole(MANAGER.getRole(), TEACHER.getRole())



                        //
                        // Event resources
                        //

                      .requestMatchers(GET, "/events")
                    .hasAnyRole(MANAGER.getRole(), TEACHER.getRole(), STUDENT.getRole())
                    .requestMatchers(PUT, "/events")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(GET, "/events/*")
                    .hasAnyRole(MANAGER.getRole(), TEACHER.getRole(), STUDENT.getRole())
                    .requestMatchers(GET, "/events/*/participants")
                    .hasAnyRole(MANAGER.getRole(), TEACHER.getRole(), STUDENT.getRole())
                    .requestMatchers(PUT, "/events/*/participants")
                    .hasAnyRole(MANAGER.getRole(), TEACHER.getRole())
                    //
                    // Attendances resources
                    //
                    .requestMatchers(GET, "/attendance")
                    .hasAnyRole(MANAGER.getRole(), TEACHER.getRole())
                    //.requestMatchers(new SelfMatcher(GET, "/attendance", "students"))
                    //.hasAnyRole(STUDENT.getRole())
                    .requestMatchers(POST, "/attendance/movement")
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(GET, STUDENT_COURSE, "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/courses/*")
                    .authenticated()
                    .requestMatchers(GET, "/courses/*/exams")
                    .authenticated()
                    .requestMatchers(GET, "/courses/*/exams/*")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(GET, "/courses/*" + "/exams/*/details")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(
                        new SelfMatcher(
                            GET, "/courses/*" + "/exams/*/participants/*", "participants"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, "/courses/*" + "/exams/*/participants/*")
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(new SelfMatcher(GET, STUDENT_COURSE, "students"))
                    .hasAnyRole(STUDENT.getRole())
                    .requestMatchers(GET, STUDENT_COURSE)
                    .hasAnyRole(TEACHER.getRole(), MANAGER.getRole())
                    .requestMatchers(PUT, STUDENT_COURSE)
                    .hasAnyRole(MANAGER.getRole())
                    .requestMatchers("/**")
                    .denyAll())

        // disable superfluous protections
        // Eg if all clients are non-browser then no csrf
        // https://docs.spring.io/spring-security/site/docs/3.2.0.CI-SNAPSHOT/reference/html/csrf.html,
        // Sec 13.3
        .cors(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable);
    // formatter:on
    return httpSecurity.build();
  }

  private Exception forbiddenWithRemoteInfo(HttpServletRequest req) {
    log.info(
        String.format(
            "Access is denied for remote caller: address=%s, host=%s, port=%s",
            req.getRemoteAddr(), req.getRemoteHost(), req.getRemotePort()));
    return new ForbiddenException("Access is denied");
  }

  private BearerAuthFilter bearerFilter(RequestMatcher requestMatcher) throws Exception {
    BearerAuthFilter bearerFilter = new BearerAuthFilter(requestMatcher, AUTHORIZATION_HEADER);
    bearerFilter.setAuthenticationManager(authenticationManager());
    bearerFilter.setAuthenticationSuccessHandler(
        (httpServletRequest, httpServletResponse, authentication) -> {});
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
