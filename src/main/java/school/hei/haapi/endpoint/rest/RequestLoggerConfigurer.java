package school.hei.haapi.endpoint.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.endpoint.rest.security.model.Principal;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.joining;

@Configuration
public class RequestLoggerConfigurer implements WebMvcConfigurer {

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new RequestLogger());
  }

  @AllArgsConstructor
  @Slf4j
  private static class RequestLogger implements HandlerInterceptor {

    private static final String THREAD_OLD_NAME = "threadOldName";
    private static final int REQUEST_ID_LENGTH = 8;
    private static final String REQUEST_START_TIME = "startTime";

    private static boolean shouldLog() {
      return isAuthenticated();
    }

    private static boolean isAuthenticated() {
      var securityContext = SecurityContextHolder.getContext();
      return securityContext != null
          && !(securityContext.getAuthentication() instanceof AnonymousAuthenticationToken);
    }

    @Override
    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler) {
      request.setAttribute(REQUEST_START_TIME, currentTimeMillis());

      Thread current = currentThread();
      String oldThreadName = current.getName();
      request.setAttribute(THREAD_OLD_NAME, oldThreadName);
      current.setName(randomUUID().toString().substring(0, REQUEST_ID_LENGTH));

      String parameters = request.getParameterMap().entrySet().stream()
          .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
          .collect(joining(";"));
      if (shouldLog()) {
        Principal principal = AuthProvider.getPrincipal();
        log.info("preHandle: "
                + "userId={}, role={}, method={}, uri={}, parameters=[{}], "
                + "handler={}, oldThreadName={}",
            principal.getUserId(), principal.getRole(),
            request.getMethod(), request.getRequestURI(), parameters, handler,
            oldThreadName);
      }
      return true;
    }

    @Override
    public void afterCompletion(
        HttpServletRequest request, HttpServletResponse response,
        Object handler, @Nullable Exception ex) {
      long duration = currentTimeMillis() - (long) request.getAttribute(REQUEST_START_TIME);
      if (shouldLog()) {
        log.info("afterCompletion: status={}, duration={}ms", response.getStatus(), duration, ex);
      }
      currentThread().setName(request.getAttribute(THREAD_OLD_NAME).toString());
    }
  }
}
