package school.hei.haapi.integration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.testData.FeeTestData.createPendingFee;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.StudentTestData.axel;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import school.hei.haapi.endpoint.rest.api.EventsApi;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.model.UserActivity;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.UserActivityRepository;
import school.hei.haapi.repository.UserRepository;

@Slf4j
class UserActivityInterceptorIT extends FacadeITMockedThirdParties {

  @Autowired private UserActivityRepository userActivityRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private FeeRepository feeRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  private User manager;
  private User student;
  private Fee fee;
  private String managerToken;
  private String studentToken;

  private final List<String> apiCreatedFeeIds = new ArrayList<>();

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    manager = userRepository.save(hasina());
    student = userRepository.save(axel());
    fee =
        feeRepository.save(createPendingFee(student, 5_000, Instant.parse("2026-06-10T08:00:00Z")));

    managerToken = tokenFor(casdoorAuthServiceMock, manager);
    studentToken = tokenFor(casdoorAuthServiceMock, student);

    await()
        .atMost(Duration.ofSeconds(3))
        .pollInterval(Duration.ofMillis(50))
        .until(
            () -> {
              userActivityRepository.deleteAll();
              return true;
            });
  }

  @AfterEach
  void tearDown() {
    List<String> feeIds = new ArrayList<>(apiCreatedFeeIds);
    feeIds.add(fee.getId());
    // Fee carries @SQLDelete, so a repository delete would only flag is_deleted: reach the tables
    // directly, children first.
    feeIds.forEach(
        feeId -> {
          jdbcTemplate.update("DELETE FROM \"fee_status_history\" WHERE fee_id = ?", feeId);
          jdbcTemplate.update("DELETE FROM \"payment\" WHERE fee_id = ?", feeId);
          jdbcTemplate.update("DELETE FROM \"fee\" WHERE id = ?", feeId);
        });
    apiCreatedFeeIds.clear();
    userRepository.deleteAll(List.of(student, manager));
  }

  private static CreateFee someCreatableFee() {
    return new CreateFee()
        .type(TUITION)
        .totalAmount(5000)
        .category(UNKNOWN)
        .frequency(FeeFrequency.UNKNOWN)
        .comment("Comment")
        .dueDatetime(Instant.parse("2026-06-10T08:00:00Z"));
  }

  @Test
  void request_on_untracked_controller_saves_no_activity() throws ApiException {
    var api = new EventsApi(anApiClient(managerToken));
    var before = userActivityRepository.count();
    api.getEvents(1, 15, null, null, null, null, null, null, null);
    await()
        .during(Duration.ofMillis(500))
        .atMost(Duration.ofSeconds(2))
        .pollInterval(Duration.ofMillis(50))
        .untilAsserted(() -> assertEquals(before, userActivityRepository.count()));
  }

  @Test
  void get_request_with_auth_saves_activity() throws ApiException {
    var api = new PayingApi(anApiClient(studentToken));
    var before = userActivityRepository.count();
    api.getStudentFeeById(student.getId(), fee.getId());
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
    var api = new PayingApi(anApiClient(managerToken));
    var before = userActivityRepository.count();
    api.createStudentFees(student.getId(), List.of(someCreatableFee()))
        .forEach(created -> apiCreatedFeeIds.add(created.getId()));
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
    var api = new PayingApi(anApiClient(managerToken));
    var before = userActivityRepository.count();
    api.getStudentFeeById(student.getId(), fee.getId());
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .until(() -> userActivityRepository.count() > before);

    var expectedPath = "/students/" + student.getId() + "/fees/" + fee.getId();
    var last = getLastActivity();
    assertTrue(
        last.getEndpoint().contains(expectedPath), "Endpoint should contain " + expectedPath);
    assertEquals("GET", last.getHttpMethod());
  }

  @Test
  void delete_request_saves_activity() throws ApiException {
    var api = new PayingApi(anApiClient(managerToken));
    var created = api.createStudentFees(student.getId(), List.of(someCreatableFee())).getFirst();
    apiCreatedFeeIds.add(created.getId());
    await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .until(() -> userActivityRepository.count() > 0);
    var before = userActivityRepository.count();
    api.deleteStudentFeeById(created.getId(), student.getId());
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
