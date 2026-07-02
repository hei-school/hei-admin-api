package school.hei.haapi.endpoint;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
public class UserActivityInterceptorConfigurer implements WebMvcConfigurer {
  private UserActivityInterceptor userActivityInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(userActivityInterceptor);
  }
}
