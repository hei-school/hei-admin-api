package school.hei.haapi.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class FeesOnlyInterceptor implements HandlerInterceptor {
    private final boolean feesOnly;
    public FeesOnlyInterceptor(@Value("${FEES_ONLY:false}") boolean feesOnly) {
        this.feesOnly = feesOnly;
    }
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        var uri = request.getRequestURI();

        if (!feesOnly || isAlwaysAllowed(uri)) {return true;}
        if (!(handler instanceof HandlerMethod handlerMethod)) {return true;}
        var allowed =
                handlerMethod.getBeanType().isAnnotationPresent(FeesOnly.class)
                        || handlerMethod.getMethod().isAnnotationPresent(FeesOnly.class);
        if (!allowed) {
            log.warn("Blocked by FEES_ONLY: {} {}", request.getMethod(), uri);
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "FEES_ONLY mode active");
            return false;
        }
        return true;
    }
    private boolean isAlwaysAllowed(String uri) {
        return uri.equals("/whoami")
                || uri.equals("/ping")
                || uri.equals("/health/db")
                || uri.startsWith("/authentication/")
                || uri.startsWith("/oauth2/")
                || uri.startsWith("/oauth2/authorization/")
                || uri.startsWith("/login")
                || uri.startsWith("/login/oauth2/")
                || uri.startsWith("/logout")
                || uri.startsWith("/error")
                || uri.startsWith("/actuator")
                || uri.startsWith("/auth/")
                || uri.startsWith("/api/auth/")
                || uri.startsWith("/callback")
                || uri.startsWith("/casdoor/");
    }
}