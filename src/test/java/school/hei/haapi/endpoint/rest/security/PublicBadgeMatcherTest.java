package school.hei.haapi.endpoint.rest.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.security.SecurityConf.PUBLIC_BADGE;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class PublicBadgeMatcherTest {
  private static final String PUBLIC_ID = "dd73d925-5c4b-47d9-ae9a-0c224c4a4a3a";

  private static MockHttpServletRequest request(String method, String path, String query) {
    var request = new MockHttpServletRequest(method, path);
    request.setServletPath(path);
    request.setQueryString(query);
    return request;
  }

  @Test
  void badge_of_public_id_is_public_whatever_the_query_string() {
    assertTrue(PUBLIC_BADGE.matches(request("GET", "/students/badges/" + PUBLIC_ID, "")));
    assertTrue(PUBLIC_BADGE.matches(request("GET", "/students/badges/" + PUBLIC_ID, null)));
    assertTrue(PUBLIC_BADGE.matches(request("GET", "/students/badges/" + PUBLIC_ID, "a=b")));
    assertTrue(
        PUBLIC_BADGE.matches(request("GET", "/students/badges/" + PUBLIC_ID.toUpperCase(), "")));
  }

  @Test
  void other_badge_endpoints_are_not_public() {
    assertFalse(PUBLIC_BADGE.matches(request("GET", "/students/badges/raw", "")));
    assertFalse(
        PUBLIC_BADGE.matches(request("GET", "/students/badges/raw", "group_id=" + PUBLIC_ID)));
    assertFalse(
        PUBLIC_BADGE.matches(request("GET", "/students/badges/" + PUBLIC_ID + "/student", "")));
    assertFalse(
        PUBLIC_BADGE.matches(request("PUT", "/students/badges/" + PUBLIC_ID + "/revocation", "")));
    assertFalse(PUBLIC_BADGE.matches(request("PUT", "/students/badges/" + PUBLIC_ID, "")));
    assertFalse(PUBLIC_BADGE.matches(request("GET", "/students/badges/not-a-uuid", "")));
  }
}
