package school.hei.haapi.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import school.hei.haapi.endpoint.rest.annotation.TrackActivity;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.service.UserActivityService;

@Slf4j
@Component
@AllArgsConstructor
public class UserActivityInterceptor implements HandlerInterceptor {
  private final UserActivityService userActivityService;

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    if (!shouldTrack(handler)) {
      return;
    }
    try {
      String userId = null;
      String userEmail = null;
      try {
        Principal principal = AuthProvider.getPrincipal();
        if (principal != null && principal.getUser() != null) {
          userId = principal.getUser().getId();
          userEmail = principal.getUser().getEmail();
        }
      } catch (Exception e) {
        log.debug("Anonymous request, no principal");
      }
      String body = null;
      var attr = request.getAttribute("cachedRequestWrapper");
      if (attr instanceof ContentCachingRequestWrapper wrapper) {
        var buf = wrapper.getContentAsByteArray();
        if (buf.length > 0) {
          body = new String(buf, StandardCharsets.UTF_8);
        }
      }
      userActivityService.save(
          userId, userEmail, request.getRequestURI(), request.getMethod(), body);
    } catch (Exception e) {
      log.error("Failed to persist user activity", e);
    }
  }

  private boolean shouldTrack(Object handler) {
    if (!(handler instanceof HandlerMethod handlerMethod)) {
      return false;
    }
    return handlerMethod.hasMethodAnnotation(TrackActivity.class)
        || handlerMethod.getBeanType().isAnnotationPresent(TrackActivity.class);
  }
}
