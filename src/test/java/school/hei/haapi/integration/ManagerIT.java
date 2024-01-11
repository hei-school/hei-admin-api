package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.endpoint.rest.model.Sex.F;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER_1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.conf.FacadeIT;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CrupdateManager;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Manager;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.TestUtils;

@Testcontainers
class ManagerIT extends FacadeIT {
  @LocalServerPort private int serverPort;

  @MockBean private SentryConf sentryConf;

  @MockBean private CognitoComponent cognitoComponentMock;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, serverPort);
  }

  public static Manager manager1() {
    Manager manager = new Manager();
    manager.setId("manager1_id");
    manager.setFirstName("One");
    manager.setLastName("Manager");
    manager.setEmail("test+manager1@hei.school");
    manager.setRef("MGR21001");
    manager.setPhone("0322411127");
    manager.setStatus(EnableStatus.ENABLED);
    manager.setSex(Sex.M);
    manager.setBirthDate(LocalDate.parse("1890-01-01"));
    manager.setEntranceDatetime(Instant.parse("2021-09-08T08:25:29Z"));
    manager.setAddress("Adr 5");
    manager.setBirthPlace("");
    manager.setNic("");
    return manager;
  }

  public static Manager disabledManager1() {
    return new Manager()
        .id("'manager2_id'")
        .firstName("Disable")
        .lastName("One")
        .email("manager+disable1@hei.school")
        .ref("MGR29001")
        .status(EnableStatus.DISABLED)
        .sex(Sex.M)
        .birthDate(LocalDate.parse("2000-12-01"))
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .phone("0322411123")
        .nic("")
        .birthPlace("")
        .address("Adr 1");
  }

  private static CrupdateManager update(Manager manager) {
    return toCrupdateManager(manager)
        .address("Adr 999")
        .sex(F)
        .lastName("Other last")
        .firstName("Other first")
        .birthDate(LocalDate.parse("2000-01-03"));
  }

  private static CrupdateManager toCrupdateManager(Manager manager) {
    return new CrupdateManager()
        .id(manager.getId())
        .firstName(manager.getFirstName())
        .lastName(manager.getLastName())
        .email(manager.getEmail())
        .ref(manager.getRef())
        .phone(manager.getPhone())
        .status(manager.getStatus())
        .sex(manager.getSex())
        .birthDate(manager.getBirthDate())
        .entranceDatetime(manager.getEntranceDatetime())
        .address(manager.getAddress())
        .birthPlace(manager.getBirthPlace())
        .nic(manager.getNic());
  }

  private static Manager toManager(CrupdateManager crupdateManager) {
    return new Manager()
        .id(crupdateManager.getId())
        .firstName(crupdateManager.getFirstName())
        .lastName(crupdateManager.getLastName())
        .email(crupdateManager.getEmail())
        .ref(crupdateManager.getRef())
        .phone(crupdateManager.getPhone())
        .status(crupdateManager.getStatus())
        .sex(crupdateManager.getSex())
        .birthDate(crupdateManager.getBirthDate())
        .entranceDatetime(crupdateManager.getEntranceDatetime())
        .address(crupdateManager.getAddress())
        .birthPlace(crupdateManager.getBirthPlace())
        .nic(crupdateManager.getNic());
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void manager_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    CrupdateManager updatedManager1 = update(manager1());

    Manager updated = api.updateManager(MANAGER_1_ID, updatedManager1);
    assertEquals(toManager(updatedManager1), updated);

    // cleanup
    api.updateManager(MANAGER_1_ID, toCrupdateManager(manager1()));
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    UsersApi api = new UsersApi(student1Client);
    assertThrowsForbiddenException(() -> api.getManagerById(MANAGER_1_ID));
    assertThrowsForbiddenException(() -> api.getManagers(1, 20, null, null));
  }

  @Test
  void teacher_read_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    UsersApi api = new UsersApi(teacher1Client);
    assertThrowsForbiddenException(() -> api.getManagerById(MANAGER_1_ID));
    assertThrowsForbiddenException(() -> api.getManagers(1, 20, null, null));
  }

  @Test
  void manager_read_own_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);

    UsersApi api = new UsersApi(manager1Client);
    Manager actual = api.getManagerById(MANAGER_1_ID);

    assertEquals(manager1(), actual);
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);

    UsersApi api = new UsersApi(manager1Client);
    List<Manager> managers = api.getManagers(1, 20, null, null);

    assertEquals(3, managers.size());
    assertEquals(manager1(), managers.get(0));
  }
}
