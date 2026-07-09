package school.hei.haapi.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.ContentCachingRequestWrapper;
import school.hei.haapi.model.TrackActivity;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.service.UserActivityService;

@Configuration
@AllArgsConstructor
public class UserActivityInterceptorConfigurer implements WebMvcConfigurer {
  private final UserActivityService userActivityService;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new UserActivityInterceptor(userActivityService));
  }

  @AllArgsConstructor
  @Slf4j
  private static class UserActivityInterceptor implements HandlerInterceptor {
    private static final String CACHED_REQUEST_WRAPPER_ATTR = "cachedRequestWrapper";
    private final UserActivityService userActivityService;

    @Override
    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler) {
      return true;
    }

    @Override
    public void afterCompletion(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        @Nullable Exception ex) {
      if (!shouldTrack(handler)) {
        return;
      }
      try {
        UserInfo userInfo = extractUserInfo();
        String body = extractBody(request);
        userActivityService.save(
            userInfo.id(), userInfo.email(), request.getRequestURI(), request.getMethod(), body);
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

    private UserInfo extractUserInfo() {
      try {
        var principal = AuthProvider.getPrincipal();
        if (principal != null && principal.getUser() != null) {
          return new UserInfo(principal.getUser().getId(), principal.getUser().getEmail());
        }
      } catch (Exception e) {
        log.debug("Anonymous request, no principal");
      }
      return new UserInfo(null, null);
    }

    private String extractBody(HttpServletRequest request) {
      var attr = request.getAttribute(CACHED_REQUEST_WRAPPER_ATTR);
      if (attr instanceof ContentCachingRequestWrapper wrapper) {
        var buf = wrapper.getContentAsByteArray();
        if (buf.length > 0) {
          return new String(buf, StandardCharsets.UTF_8);
        }
      }
      return null;
    }

    private record UserInfo(String id, String email) {}
  }
}
