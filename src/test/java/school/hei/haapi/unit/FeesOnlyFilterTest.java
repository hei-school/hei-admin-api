package school.hei.haapi.unit;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import school.hei.haapi.endpoint.rest.security.FeesOnlyFilter;

@ExtendWith(MockitoExtension.class)
class FeesOnlyFilterTest {

    @Mock private FilterChain filterChain;

    @Test
    void fees_only_inactive_all_requests_pass() throws ServletException, IOException {
        FeesOnlyFilter filter = new FeesOnlyFilter(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/teachers");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void fees_only_active_allows_configured_prefixes() throws ServletException, IOException {
        FeesOnlyFilter filter = new FeesOnlyFilter(true);

        List<String> allowedPrefixes =
                List.of("/fees", "/students", "/whoami", "/ping", "/authentication", "/health", "/mpbs");

        for (String prefix : allowedPrefixes) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI(prefix);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertEquals(
                    200, response.getStatus(), "URI " + prefix + " should be allowed in FEES_ONLY mode");
        }
    }

    @Test
    void fees_only_active_blocks_other_uris() throws ServletException, IOException {
        FeesOnlyFilter filter = new FeesOnlyFilter(true);

        List<String> blockedUris =
                List.of("/teachers", "/groups", "/events", "/courses", "/exams", "/unknown");

        for (String uri : blockedUris) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI(uri);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertEquals(SC_FORBIDDEN, response.getStatus(), "URI " + uri + " should be blocked");
            assertEquals(
                    "{\"message\": \"This endpoint is disabled in FEES_ONLY mode\"}",
                    response.getContentAsString());
        }
    }

    @Test
    void fees_only_active_allows_subpaths_of_allowed_prefixes() throws ServletException, IOException {
        FeesOnlyFilter filter = new FeesOnlyFilter(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/students/student1_id/fees/fee1_id/payments");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void fees_only_inactive_allows_previously_blocked_uris() throws ServletException, IOException {
        FeesOnlyFilter filter = new FeesOnlyFilter(false);

        List<String> uris =
                List.of("/teachers", "/groups", "/events", "/courses", "/exams", "/unknown");

        for (String uri : uris) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI(uri);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            assertEquals(200, response.getStatus(), "URI " + uri + " should pass when FEES_ONLY is inactive");
        }
    }

    @Test
    void fees_only_inactive_does_not_write_forbidden_body() throws ServletException, IOException {
        FeesOnlyFilter filter = new FeesOnlyFilter(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/exams");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals("", response.getContentAsString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void fees_only_inactive_calls_filter_chain_for_every_request() throws ServletException, IOException {
        FeesOnlyFilter filter = new FeesOnlyFilter(false);

        List<String> uris = List.of("/fees", "/students", "/teachers", "/anything/goes/here");

        for (String uri : uris) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI(uri);
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    void fees_only_inactive_allows_root_and_empty_like_uris() throws ServletException, IOException {
        FeesOnlyFilter filter = new FeesOnlyFilter(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void fees_only_active_does_not_call_filter_chain_when_blocked() throws ServletException, IOException {
        FeesOnlyFilter filter = new FeesOnlyFilter(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/teachers");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void fees_only_active_blocked_response_has_json_content_type() throws ServletException, IOException {
        FeesOnlyFilter filter = new FeesOnlyFilter(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/teachers");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals("application/json", response.getContentType());
    }

    @Test
    void fees_only_active_blocks_uris_that_merely_start_with_allowed_prefix() throws ServletException, IOException {
        FeesOnlyFilter filter = new FeesOnlyFilter(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/feesnotreal");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(
                SC_FORBIDDEN,
                response.getStatus(),
                "'/feesnotreal' should not be treated as a valid subpath of '/fees'");
    }

    @Test
    void fees_only_active_is_case_sensitive_on_prefixes() throws ServletException, IOException {
        FeesOnlyFilter filter = new FeesOnlyFilter(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/FEES/invoice1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(
                SC_FORBIDDEN,
                response.getStatus(),
                "startsWith() is case sensitive: '/FEES' does not match the '/fees' prefix");
    }

    @Test
    void fees_only_active_blocks_empty_uri() throws ServletException, IOException {
        FeesOnlyFilter filter = new FeesOnlyFilter(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(SC_FORBIDDEN, response.getStatus());
    }
}