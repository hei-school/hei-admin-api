package school.hei.haapi.unit;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import school.hei.haapi.endpoint.rest.security.FeesOnlyFilter;

@ExtendWith(MockitoExtension.class)
class FeesOnlyFilterTest {

    private static final String FORBIDDEN_BODY =
            "{\"message\": \"This endpoint is disabled in FEES_ONLY mode\"}";

    @Mock private FilterChain filterChain;

    private FeesOnlyFilter enabledFilter() {
        return new FeesOnlyFilter(true);
    }

    private FeesOnlyFilter disabledFilter() {
        return new FeesOnlyFilter(false);
    }

    private MockHttpServletRequest request(String uri) {
        var request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }

    private MockHttpServletResponse response() {
        return new MockHttpServletResponse();
    }

    private void assertAllowed(String uri, FeesOnlyFilter filter)
            throws ServletException, IOException {

        var request = request(uri);
        var response = response();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    private void assertBlocked(String uri, FeesOnlyFilter filter)
            throws ServletException, IOException {

        var request = request(uri);
        var response = response();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(SC_FORBIDDEN, response.getStatus());
        assertEquals(FORBIDDEN_BODY, response.getContentAsString());
        assertEquals("application/json", response.getContentType());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void fees_only_inactive_all_requests_pass() throws Exception {
        assertAllowed("/teachers", disabledFilter());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"/fees", "/students", "/whoami", "/ping", "/authentication", "/health", "/mpbs"})
    void fees_only_active_allows_configured_prefixes(String uri) throws Exception {
        assertAllowed(uri, enabledFilter());
    }

    @Test
    void fees_only_active_allows_subpaths_of_allowed_prefixes() throws Exception {
        assertAllowed("/students/student1_id/fees/fee1_id/payments", enabledFilter());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "/teachers",
                    "/groups",
                    "/events",
                    "/courses",
                    "/exams",
                    "/unknown",
                    "",
                    "/feesnotreal",
                    "/FEES/invoice1"
            })
    void fees_only_active_blocks_invalid_uris(String uri) throws Exception {
        assertBlocked(uri, enabledFilter());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/teachers", "/groups", "/events", "/courses", "/exams", "/unknown"})
    void fees_only_inactive_allows_previously_blocked_uris(String uri) throws Exception {
        assertAllowed(uri, disabledFilter());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/fees", "/students", "/teachers", "/anything/goes/here"})
    void fees_only_inactive_calls_filter_chain_for_every_request(String uri) throws Exception {
        assertAllowed(uri, disabledFilter());
    }

    @Test
    void fees_only_inactive_does_not_write_forbidden_body() throws Exception {

        var request = request("/exams");
        var response = response();

        disabledFilter().doFilterInternal(request, response, filterChain);

        assertEquals("", response.getContentAsString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void fees_only_inactive_allows_root_uri() throws Exception {
        assertAllowed("/", disabledFilter());
    }
}
