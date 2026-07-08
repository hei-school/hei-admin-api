package school.hei.haapi.integration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static school.hei.haapi.integration.conf.TestUtils.*;

import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.EventsApi;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
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
    await()
        .atMost(Duration.ofSeconds(3))
        .pollInterval(Duration.ofMillis(50))
        .until(
            () -> {
              userActivityRepository.deleteAll();
              return true;
            });
  }

  @Test
  void request_on_untracked_controller_saves_no_activity() throws Exception {
    var api = new EventsApi(anApiClient(MANAGER1_TOKEN));
    var before = userActivityRepository.count();
    api.getEvents(1, 15, null, null, null, null, null, null, null);
    Thread.sleep(500);
    assertEquals(before, userActivityRepository.count());
  }

  @Test
  void get_request_with_auth_saves_activity() throws ApiException {
    var api = new PayingApi(anApiClient(STUDENT1_TOKEN));
    var before = userActivityRepository.count();

    api.getStudentFeeById(STUDENT1_ID, FEE1_ID);

    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .until(() -> userActivityRepository.count() > before);

    var last = getLastActivity();
    assertEquals("GET", last.getHttpMethod());
    assertNotNull(last.getUserId());
    assertNotNull(last.getUserEmail());
  }

  @Test
  void post_request_saves_activity_with_request_body() throws ApiException {
    var api = new PayingApi(anApiClient(MANAGER1_TOKEN));
    var before = userActivityRepository.count();
    api.createStudentFees(STUDENT1_ID, List.of(createFeeForTest()));
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .until(() -> userActivityRepository.count() > before);

    var last = getLastActivity();
    assertEquals("POST", last.getHttpMethod());
    assertNotNull(last.getRequestBody());
    assertFalse(last.getRequestBody().isBlank());
  }

  @Test
  void activity_saves_correct_endpoint_and_http_method() throws ApiException {
    var api = new PayingApi(anApiClient(MANAGER1_TOKEN));
    var before = userActivityRepository.count();
    api.getStudentFeeById(STUDENT1_ID, FEE1_ID);
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .until(() -> userActivityRepository.count() > before);

    var last = getLastActivity();
    assertTrue(
        last.getEndpoint().contains("/students/" + STUDENT1_ID + "/fees/" + FEE1_ID),
        "Endpoint should contain /students/" + STUDENT1_ID + "/fees/" + FEE1_ID);
    assertEquals("GET", last.getHttpMethod());
  }

  @Test
  void delete_request_saves_activity() throws ApiException {
    var api = new PayingApi(anApiClient(MANAGER1_TOKEN));
    var created = api.createStudentFees(STUDENT1_ID, List.of(createFeeForTest())).getFirst();
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .until(() -> userActivityRepository.count() > 0);
    var before = userActivityRepository.count();
    api.deleteStudentFeeById(created.getId(), STUDENT1_ID);
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .until(() -> userActivityRepository.count() > before);

    var last = getLastActivity();
    assertEquals("DELETE", last.getHttpMethod());
  }

  private UserActivity getLastActivity() {
    var all = userActivityRepository.findAll();
    assertFalse(all.isEmpty(), "No activity detected");
    return all.get(all.size() - 1);
  }
}
