package school.hei.haapi.endpoint.rest.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class FeesOnlySecurityConfig {

    private final FeesOnlyUriMatcher feesOnlyUriMatcher;

    @Value("${FEES_ONLY:false}")
    private boolean feesOnly;

    public FeesOnlySecurityConfig(FeesOnlyUriMatcher feesOnlyUriMatcher) {
        this.feesOnlyUriMatcher = feesOnlyUriMatcher;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain feesOnlyFilterChain(HttpSecurity http) throws Exception {
        if (!feesOnly) {
            http.securityMatcher(request -> false)
                    .authorizeHttpRequests(req -> req.anyRequest().denyAll());
            return http.build();
        }

        http.securityMatcher(request -> !feesOnlyUriMatcher.isAllowed(request.getRequestURI()))
                .authorizeHttpRequests(req -> req.anyRequest().denyAll())
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
