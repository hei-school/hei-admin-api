package school.hei.haapi.endpoint;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.joining;
import static school.hei.haapi.concurrency.ThreadRenamer.renameFrontalThread;
import static school.hei.haapi.concurrency.ThreadRenamer.renameThread;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
@Configuration
@AllArgsConstructor
public class RequestLoggerConfigurer implements WebMvcConfigurer {
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new RequestLogger());
  }

  @PojaGenerated
  @AllArgsConstructor
  @Slf4j
  private static class RequestLogger implements HandlerInterceptor {

    private static final String THREAD_OLD_NAME = "threadOldName";
    private static final String REQUEST_START_TIME = "startTime";

    @Override
    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler) {
      request.setAttribute(REQUEST_START_TIME, currentTimeMillis());

      Thread current = currentThread();
      String oldThreadName = current.getName();
      request.setAttribute(THREAD_OLD_NAME, oldThreadName);
      renameFrontalThread(current);

      String parameters =
          request.getParameterMap().entrySet().stream()
              .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
              .collect(joining(";"));
      log.info(
          "preHandle: " + "method={}, uri={}, parameters=[{}], " + "handler={}, oldThreadName={}",
          request.getMethod(),
          request.getRequestURI(),
          parameters,
          handler,
          oldThreadName);
      return true;
    }

    @Override
    public void afterCompletion(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        @Nullable Exception ex) {
      long duration = currentTimeMillis() - (long) request.getAttribute(REQUEST_START_TIME);
      log.info("afterCompletion: status={}, duration={}ms", response.getStatus(), duration, ex);
      renameThread(currentThread(), request.getAttribute(THREAD_OLD_NAME).toString());
    }
  }
}
