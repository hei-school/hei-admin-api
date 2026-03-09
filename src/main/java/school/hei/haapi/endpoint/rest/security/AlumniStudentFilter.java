package school.hei.haapi.endpoint.rest.security;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static school.hei.haapi.model.User.Status.ALUMNI;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@AllArgsConstructor
@Slf4j
public class AlumniStudentFilter extends OncePerRequestFilter {
    private final RequestMatcher requiresNonAlumniStudentRequestMatchers;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (requiresNonAlumniStudentRequestMatchers.matches(request)) {
            var principal = AuthProvider.getPrincipal();
            if (ALUMNI.equals(principal.getStatus())) {
                response.sendError(SC_FORBIDDEN, "access is denied");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
