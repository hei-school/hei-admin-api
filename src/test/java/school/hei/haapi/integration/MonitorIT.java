package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.Sex.F;
import static school.hei.haapi.endpoint.rest.model.Sex.M;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.coordinatesWithNullValues;
import static school.hei.haapi.integration.conf.TestUtils.coordinatesWithValues;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.endpoint.rest.model.CrupdateMonitor;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Monitor;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MonitorIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class MonitorIT extends MockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, MonitorIT.ContextInitializer.SERVER_PORT);
  }

  public static Monitor monitor1() {
    Monitor monitor = new Monitor();
    monitor.setId("monitor1_id");
    monitor.setFirstName("Monitor");
    monitor.setLastName("One");
    monitor.setEmail("test+monitor@hei.school");
    monitor.setRef("MTR21001");
    monitor.setPhone("0322411123");
    monitor.setStatus(ENABLED);
    monitor.setSex(M);
    monitor.setBirthDate(LocalDate.parse("2000-01-01"));
    monitor.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    monitor.setAddress("Adr 1");
    monitor.setNic("");
    monitor.setBirthPlace("");
    monitor.coordinates(monitor1Coordinates());
    monitor.setHighSchoolOrigin("Lycée Andohalo");
    return monitor;
  }

  public static Monitor expectedCreated() {
    Monitor monitor = new Monitor();
    monitor.setId("monitor2_id");
    monitor.setFirstName("Monitor");
    monitor.setLastName("One");
    monitor.setEmail("test+monitor2@hei.school");
    monitor.setRef("MTR21002");
    monitor.setPhone("0322411123");
    monitor.setStatus(ENABLED);
    monitor.setSex(F);
    monitor.setBirthDate(LocalDate.parse("2000-01-01"));
    monitor.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    monitor.setAddress("Adr 6");
    monitor.setNic("");
    monitor.setBirthPlace("");
    monitor.coordinates(coordinatesWithNullValues());
    monitor.setHighSchoolOrigin(null);
    return monitor;
  }

  public static CrupdateMonitor someCreatableMonitor2() {
    CrupdateMonitor monitor = new CrupdateMonitor();
    monitor.setId("monitor2_id");
    monitor.setFirstName("Monitor");
    monitor.setLastName("One");
    monitor.setEmail("test+monitor2@hei.school");
    monitor.setRef("MTR21002");
    monitor.setPhone("0322411123");
    monitor.setStatus(ENABLED);
    monitor.setSex(F);
    monitor.setBirthDate(LocalDate.parse("2000-01-01"));
    monitor.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    monitor.setAddress("Adr 6");
    monitor.setNic("");
    monitor.setBirthPlace("");
    monitor.coordinates(coordinatesWithNullValues());
    monitor.setHighSchoolOrigin(null);
    return monitor;
  }

  public static CrupdateMonitor someUpdatableMonitor1() {
    return new CrupdateMonitor()
        .id("monitor1_id")
        .email("test+monitor@hei.school")
        .ref("MTR21001")
        .phone("0322411123")
        .status(EnableStatus.ENABLED)
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

  public static Coordinates monitor1Coordinates() {
    return new Coordinates().longitude(-123.123).latitude(123.0);
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

  @Test
  void manager_write_monitor_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Monitor> monitors = api.createOrUpdateMonitors(List.of(someCreatableMonitor2()));
    Monitor actualCreated = monitors.get(0);

    assertEquals(1, monitors.size());
    assertEquals(expectedCreated().getAddress(), actualCreated.getAddress());
    assertEquals(expectedCreated().getBirthDate(), actualCreated.getBirthDate());
    assertEquals(expectedCreated().getEmail(), actualCreated.getEmail());
    assertEquals(expectedCreated().getBirthPlace(), actualCreated.getBirthPlace());
    assertEquals(expectedCreated().getFirstName(), actualCreated.getFirstName());
    assertEquals(expectedCreated().getAddress(), actualCreated.getAddress());
    assertEquals(expectedCreated().getLastName(), actualCreated.getLastName());
  }

  @Test
  void manager_read_monitors_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Monitor> actual = api.getMonitors(1, 10, null, null, null);

    assertEquals(1, actual.size());
    assertEquals(monitor1(), actual.getFirst());
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
