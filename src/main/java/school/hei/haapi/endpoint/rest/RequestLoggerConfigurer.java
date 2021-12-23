package school.hei.haapi.endpoint.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

  @Slf4j
  private static class RequestLogger implements HandlerInterceptor {

    private static final String THREAD_OLD_NAME = "threadOldName";
    private static final int REQUEST_ID_LENGTH = 10;
    private static final String REQUEST_START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
      request.setAttribute(REQUEST_START_TIME, currentTimeMillis());

      Thread current = currentThread();
      request.setAttribute(THREAD_OLD_NAME, current.getName());
      current.setName(randomUUID().toString().substring(0, REQUEST_ID_LENGTH));

      String parameters = request.getParameterMap().entrySet().stream()
          .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
          .collect(joining(";"));
      log.info("preHandle: method={}, uri={}, parameters=[{}], handler={}",
          request.getMethod(), request.getRequestURI(), parameters, handler);
      return true;
    }

    @Override
    public void afterCompletion(
        HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
      long duration = currentTimeMillis() - (long) request.getAttribute(REQUEST_START_TIME);
      log.info("afterCompletion: status={}, duration={}ms",
          response.getStatus(), duration, ex);
      currentThread().setName(request.getAttribute(THREAD_OLD_NAME).toString());
    }
  }
}
