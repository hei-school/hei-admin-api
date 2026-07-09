package school.hei.haapi.endpoint;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
public class FeesOnlyInterceptorConfigurer implements WebMvcConfigurer {

    private final FeesOnlyInterceptor feesOnlyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(feesOnlyInterceptor)
                .addPathPatterns("/**");
    }
}