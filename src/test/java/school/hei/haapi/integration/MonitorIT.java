package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.Sex.F;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfFreddy;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.endpoint.rest.model.CrupdateMonitor;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public class MonitorIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private UserRepository userRepository;

  private User monitorOne;
  private User monitorTwo;
  private User managerHasina;
  private User studentAxel;
  private User teacherToky;

  private String monitorToken;
  private String managerToken;
  private String studentToken;
  private String teacherToken;

  private void setUpTestData() {
    monitorOne = userRepository.save(monitorOfAxel());
    monitorTwo = userRepository.save(monitorOfFreddy());
    managerHasina = userRepository.save(hasina());
    studentAxel = userRepository.save(axel());
    teacherToky = userRepository.save(toky());
  }

  @BeforeEach
  public void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpTestData();

    monitorToken = tokenFor(casdoorAuthServiceMock, monitorOne);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    studentToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll(
        List.of(monitorOne, monitorTwo, managerHasina, studentAxel, teacherToky));
  }

  private UsersApi apiAs(String token) {
    return new UsersApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private static CrupdateMonitor aCreatableMonitor() {
    return new CrupdateMonitor()
        .id(randomUUID().toString())
        .firstName("Monitor")
        .lastName("One")
        .email("test+monitor-" + randomUUID() + "@hei.school")
        .ref("MTR" + randomUUID())
        .phone("0322411123")
        .status(ENABLED)
        .sex(F)
        .birthDate(LocalDate.parse("2000-01-01"))
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .address("Adr 6")
        .nic("")
        .birthPlace("")
        .coordinates(new Coordinates().latitude(null).longitude(null));
  }

  @Test
  void monitor_read_own_ok() throws ApiException {
    var actual = apiAs(monitorToken).getMonitorById(monitorOne.getId());

    assertEquals(monitorOne.getId(), actual.getId());
    assertEquals(monitorOne.getRef(), actual.getRef());
    assertEquals(monitorOne.getEmail(), actual.getEmail());
  }

  @Test
  void manager_read_ok() throws ApiException {
    var actual = apiAs(managerToken).getMonitorById(monitorOne.getId());

    assertEquals(monitorOne.getId(), actual.getId());
    assertEquals(monitorOne.getRef(), actual.getRef());
  }

  @Test
  void monitor_read_other_ko() {
    var api = apiAs(monitorToken);

    assertThrowsForbiddenException(() -> api.getMonitorById(monitorTwo.getId()));
  }

  @Test
  void student_read_ko() {
    var api = apiAs(studentToken);

    assertThrowsForbiddenException(() -> api.getMonitorById(monitorOne.getId()));
  }

  @Test
  void teacher_read_ko() {
    var api = apiAs(teacherToken);

    assertThrowsForbiddenException(() -> api.getMonitorById(monitorOne.getId()));
  }

  @Test
  void manager_write_monitor_ok() throws ApiException {
    var api = apiAs(managerToken);
    var toCreate = aCreatableMonitor();

    var monitors = api.createOrUpdateMonitors(List.of(toCreate));
    var actualCreated = monitors.getFirst();

    assertEquals(1, monitors.size());
    assertEquals(toCreate.getAddress(), actualCreated.getAddress());
    assertEquals(toCreate.getBirthDate(), actualCreated.getBirthDate());
    assertEquals(toCreate.getEmail(), actualCreated.getEmail());
    assertEquals(toCreate.getBirthPlace(), actualCreated.getBirthPlace());
    assertEquals(toCreate.getFirstName(), actualCreated.getFirstName());
    assertEquals(toCreate.getLastName(), actualCreated.getLastName());

    var toUpdate =
        aCreatableMonitor()
            .id(actualCreated.getId())
            .email(actualCreated.getEmail())
            .ref(actualCreated.getRef())
            .sex(Sex.M)
            .address("Adr 111")
            .lastName("Other lastname")
            .firstName("Other firstname")
            .coordinates(new Coordinates().longitude(10.0).latitude(10.0));

    var updated = api.updateMonitorById(actualCreated.getId(), toUpdate);
    var reread = api.getMonitorById(actualCreated.getId());

    assertEquals(updated, reread);
    assertEquals("Other lastname", reread.getLastName());

    userRepository.deleteById(actualCreated.getId());
  }

  @Test
  void manager_read_monitors_ok() throws ApiException {
    var api = apiAs(managerToken);

    var actualMonitorOne = api.getMonitorById(monitorOne.getId());
    var allMonitors = api.getMonitors(1, 100, null, null, null);

    assertEquals(monitorOne.getId(), actualMonitorOne.getId());
    assertTrue(allMonitors.stream().anyMatch(m -> monitorOne.getId().equals(m.getId())));
    assertTrue(allMonitors.stream().anyMatch(m -> monitorTwo.getId().equals(m.getId())));
  }
}
