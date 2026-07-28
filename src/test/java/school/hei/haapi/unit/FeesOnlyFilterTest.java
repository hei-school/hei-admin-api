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
    var filter = new FeesOnlyFilter(false);

    var request = new MockHttpServletRequest();
    request.setRequestURI("/teachers");
    var response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    assertEquals(200, response.getStatus());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void fees_only_active_allows_configured_prefixes() throws ServletException, IOException {
    var filter = new FeesOnlyFilter(true);

    var allowedPrefixes =
        List.of("/fees", "/students", "/whoami", "/ping", "/authentication", "/health", "/mpbs");

    for (var prefix : allowedPrefixes) {
      var request = new MockHttpServletRequest();
      request.setRequestURI(prefix);
      var response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      assertEquals(
          200, response.getStatus(), "URI " + prefix + " should be allowed in FEES_ONLY mode");
    }
  }

  @Test
  void fees_only_active_blocks_other_uris() throws ServletException, IOException {
    var filter = new FeesOnlyFilter(true);

    var blockedUris = List.of("/teachers", "/groups", "/events", "/courses", "/exams", "/unknown");

    for (var uri : blockedUris) {
      var request = new MockHttpServletRequest();
      request.setRequestURI(uri);
      var response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      assertEquals(SC_FORBIDDEN, response.getStatus(), "URI " + uri + " should be blocked");
      assertEquals(
          "{\"message\": \"This endpoint is disabled in FEES_ONLY mode\"}",
          response.getContentAsString());
    }
  }

  @Test
  void fees_only_active_allows_subpaths_of_allowed_prefixes() throws ServletException, IOException {
    var filter = new FeesOnlyFilter(true);

    var request = new MockHttpServletRequest();
    request.setRequestURI("/students/student1_id/fees/fee1_id/payments");
    var response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    assertEquals(200, response.getStatus());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void fees_only_inactive_allows_previously_blocked_uris() throws ServletException, IOException {
    var filter = new FeesOnlyFilter(false);

    var uris = List.of("/teachers", "/groups", "/events", "/courses", "/exams", "/unknown");

    for (var uri : uris) {
      var request = new MockHttpServletRequest();
      request.setRequestURI(uri);
      var response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      assertEquals(
          200, response.getStatus(), "URI " + uri + " should pass when FEES_ONLY is inactive");
    }
  }

  @Test
  void fees_only_inactive_does_not_write_forbidden_body() throws ServletException, IOException {
    var filter = new FeesOnlyFilter(false);

    var request = new MockHttpServletRequest();
    request.setRequestURI("/exams");
    var response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    assertEquals("", response.getContentAsString());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void fees_only_inactive_calls_filter_chain_for_every_request()
      throws ServletException, IOException {
    var filter = new FeesOnlyFilter(false);

    var uris = List.of("/fees", "/students", "/teachers", "/anything/goes/here");

    for (var uri : uris) {
      var request = new MockHttpServletRequest();
      request.setRequestURI(uri);
      var response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      verify(filterChain).doFilter(request, response);
    }
  }

  @Test
  void fees_only_inactive_allows_root_and_empty_like_uris() throws ServletException, IOException {
    var filter = new FeesOnlyFilter(false);

    var request = new MockHttpServletRequest();
    request.setRequestURI("/");
    var response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    assertEquals(200, response.getStatus());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void fees_only_active_does_not_call_filter_chain_when_blocked()
      throws ServletException, IOException {
    var filter = new FeesOnlyFilter(true);

    var request = new MockHttpServletRequest();
    request.setRequestURI("/teachers");
    var response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void fees_only_active_blocked_response_has_json_content_type()
      throws ServletException, IOException {
    var filter = new FeesOnlyFilter(true);

    var request = new MockHttpServletRequest();
    request.setRequestURI("/teachers");
    var response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    assertEquals("application/json", response.getContentType());
  }

  @Test
  void fees_only_active_blocks_uris_that_merely_start_with_allowed_prefix()
      throws ServletException, IOException {
    var filter = new FeesOnlyFilter(true);

    var request = new MockHttpServletRequest();
    request.setRequestURI("/feesnotreal");
    var response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    assertEquals(
        SC_FORBIDDEN,
        response.getStatus(),
        "'/feesnotreal' should not be treated as a valid subpath of '/fees'");
  }

  @Test
  void fees_only_active_is_case_sensitive_on_prefixes() throws ServletException, IOException {
    var filter = new FeesOnlyFilter(true);

    var request = new MockHttpServletRequest();
    request.setRequestURI("/FEES/invoice1");
    var response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    assertEquals(
        SC_FORBIDDEN,
        response.getStatus(),
        "startsWith() is case sensitive: '/FEES' does not match the '/fees' prefix");
  }

  @Test
  void fees_only_active_blocks_empty_uri() throws ServletException, IOException {
    var filter = new FeesOnlyFilter(true);

    var request = new MockHttpServletRequest();
    request.setRequestURI("");
    var response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    assertEquals(SC_FORBIDDEN, response.getStatus());
  }
}
