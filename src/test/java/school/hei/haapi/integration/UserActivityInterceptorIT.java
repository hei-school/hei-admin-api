package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.*;
import static school.hei.haapi.integration.conf.TestUtils.*;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.EventsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.UserActivity;
import school.hei.haapi.repository.UserActivityRepository;

@Slf4j
@Testcontainers
@AutoConfigureMockMvc
class UserActivityInterceptorIT extends FacadeITMockedThirdParties {

  @Autowired private UserActivityRepository userActivityRepository;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    userActivityRepository.deleteAll();
  }

  @Test
  void request_always_passes_and_activity_is_saved() throws Exception {
    var api = new EventsApi(anApiClient(null));
    var before = userActivityRepository.count();
    api.getEvents(1, 15, null, null, null, null, null, null, null);
    var after = userActivityRepository.count();

    assertEquals(before + 1, after);
  }

  @Test
  void get_request_with_auth_saves_activity() throws Exception {
    var api = new EventsApi(anApiClient(MANAGER1_TOKEN));
    var before = userActivityRepository.count();
    api.getEventById(EVENT1_ID);
    var after = userActivityRepository.count();

    assertEquals(before + 1, after);

    var last = getLastActivity();
    assertEquals("GET", last.getHttpMethod());
    assertNotNull(last.getUserId());
    assertNotNull(last.getUserEmail());
  }

  @Test
  void post_request_saves_activity_with_request_body() throws Exception {
    var api = new EventsApi(anApiClient(MANAGER1_TOKEN));
    var before = userActivityRepository.count();
    api.crupdateEvents(List.of(createEventCourse1()), null, null, null, null);
    var after = userActivityRepository.count();

    assertEquals(before + 1, after);

    var last = getLastActivity();
    assertEquals("PUT", last.getHttpMethod());
    assertNotNull(last.getRequestBody());
    assertFalse(last.getRequestBody().isBlank());
  }

  @Test
  void request_without_auth_saves_activity_with_null_user_fields() throws Exception {
    var api = new EventsApi(anApiClient(null));

    api.getEvents(1, 15, null, null, null, null, null, null, null);

    var last = getLastActivity();
    assertNull(last.getUserId(), "userId must be null without authentification");
    assertNull(last.getUserEmail(), "userEmail must be null without authentification");
  }

  @Test
  void activity_saves_correct_endpoint_and_http_method() throws Exception {
    var api = new EventsApi(anApiClient(MANAGER1_TOKEN));
    api.getEventById(EVENT1_ID);

    var last = getLastActivity();
    assertTrue(
        last.getEndpoint().contains("/events/" + EVENT1_ID), "be content /events/" + EVENT1_ID);
    assertEquals("GET", last.getHttpMethod());
  }

  @Test
  void delete_request_saves_activity() throws Exception {
    var api = new EventsApi(anApiClient(MANAGER1_TOKEN));

    var created =
        api.crupdateEvents(List.of(createEventCourse1()), null, null, null, null).getFirst();

    var before = userActivityRepository.count();
    api.deleteEventById(created.getId());
    var after = userActivityRepository.count();
    assertEquals(before + 1, after);
    var last = getLastActivity();

    assertEquals("DELETE", last.getHttpMethod());
  }

  private UserActivity getLastActivity() {
    var all = userActivityRepository.findAll();
    assertFalse(all.isEmpty(), "Aucune activité trouvée en base");
    UserActivity last = all.get(all.size() - 1);
    return last;
  }
}
