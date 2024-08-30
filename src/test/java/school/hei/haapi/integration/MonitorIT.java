package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.coordinatesWithValues;
import static school.hei.haapi.integration.conf.TestUtils.monitor1;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CrupdateMonitor;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Monitor;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = TeacherIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class MonitorIT extends MockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;

  @Autowired private ObjectMapper objectMapper;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, TeacherIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
  }

  @Test
  void monitor_read_own_ok() throws ApiException {
    ApiClient monitor1Client = anApiClient(MONITOR1_TOKEN);

    UsersApi api = new UsersApi(monitor1Client);
    Monitor actual = api.getMonitorById(MONITOR1_ID);

    assertEquals(monitor1(), actual);
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);

    UsersApi api = new UsersApi(manager1Client);
    Monitor actual = api.getMonitorById(MONITOR1_ID);

    assertEquals(monitor1(), actual);
  }

  @Test
  void monitor_read_other_ko() {
    ApiClient monitor1Client = anApiClient(MONITOR1_TOKEN);

    UsersApi api = new UsersApi(monitor1Client);
    assertThrowsForbiddenException(() -> api.getMonitorById("monitor2_id"));
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    UsersApi api = new UsersApi(student1Client);
    assertThrowsForbiddenException(() -> api.getMonitorById(MONITOR1_ID));
  }

  @Test
  void teacher_read_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    UsersApi api = new UsersApi(teacher1Client);
    assertThrowsForbiddenException(() -> api.getMonitorById(MONITOR1_ID));
  }

  @Test
  @DirtiesContext
  void manager_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);

    UsersApi api = new UsersApi(manager1Client);
    Monitor actual = api.updateMonitorById(MONITOR1_ID, someUpdatableMonitor1());
    Monitor expected = api.getMonitorById(MONITOR1_ID);

    assertEquals(actual, expected);
  }

  public static CrupdateMonitor someUpdatableMonitor1() {
    return new CrupdateMonitor()
        .id("monitor1_id")
        .email("test+monitor@hei.school")
        .ref("MTR21001")
        .phone("0322411123")
        .status(EnableStatus.ENABLED)
        .sex(Sex.F)
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .nic("")
        .birthPlace("")
        .address("Adr 111")
        .sex(Sex.M)
        .lastName("Other lastname")
        .firstName("Other firstname")
        .birthDate(LocalDate.parse("2000-01-01"))
        .coordinates(coordinatesWithValues());
  }
}
