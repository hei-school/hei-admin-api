package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Scanner;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = ScannerIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class ScannerIT {
  @MockBean private SentryConf sentryConfMock;

  @MockBean private CognitoComponent cognitoComponentMock;

  @MockBean EventBridgeClient eventBridgeClientMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    UsersApi api = new UsersApi(student1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdateScannerUsers(List.of()));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    UsersApi api = new UsersApi(teacher1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdateScannerUsers(List.of()));
  }

  @Test
  void manager_write_update_more_than_10_scanner_users_ko() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    Scanner scannerToCreate = scannerUserToCreate();
    List<Scanner> listToCreate = someCreatableScanner(11);
    listToCreate.add(scannerToCreate);

    assertThrowsApiException(
        "{\"type\":\"500 INTERNAL_SERVER_ERROR\",\"message\":\"Request entries must be <= 10\"}",
        () -> api.createOrUpdateScannerUsers(listToCreate));

    List<Teacher> actual = api.getTeachers(1, 20, null, null, null);
    assertFalse(
        actual.stream().anyMatch(s -> Objects.equals(scannerToCreate.getEmail(), s.getEmail())));
  }

  @Test
  void manager_read_scanner_users_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi usersApi = new UsersApi(manager1Client);

    List<Scanner> actual = usersApi.getScannerUsers(1, 20, null, null, null);
    assertEquals(scannerUser(), actual.get(0));
    assertTrue(actual.contains(scannerUser()));
  }

  @Test
  void manager_create_scanner_users_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi usersApi = new UsersApi(manager1Client);
    Scanner toCreate = scannerUserToCreate();

    List<Scanner> actual = usersApi.createOrUpdateScannerUsers(List.of(toCreate));
    assertEquals(1, actual.size());
    Scanner created = actual.get(0);
    assertTrue(isValidUUID(created.getId()));
    toCreate.setId(created.getId());
    assertEquals(created, toCreate);
  }

  @Test
  void manager_read_scanner_user_by_id_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi usersApi = new UsersApi(manager1Client);

    Scanner actual = usersApi.getScannerUserById(scannerUser().getId());
    assertEquals(scannerUser(), actual);
  }

  public static List<Scanner> someCreatableScanner(int nbOfScanner) {
    List<Scanner> scannerList = new ArrayList<>();
    for (int i = 0; i < nbOfScanner; i++) {
      scannerList.add(scannerUserToCreate());
    }
    return scannerList;
  }

  public static Scanner scannerUser() {
    return new Scanner()
        .id("scanner1_id")
        .phone("0340000000")
        .email("test+scanner@hei.school")
        .status(EnableStatus.ENABLED)
        .ref("SCN20001")
        .sex(Scanner.SexEnum.M)
        .lastName("Scanner")
        .firstName("Test")
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .birthDate(LocalDate.parse("2000-01-01"))
        .address("Adr 1");
  }

  public static Scanner scannerUserToCreate() {
    return new Scanner()
        .phone("0320000000")
        .status(EnableStatus.ENABLED)
        .ref("SCN" + randomUUID())
        .sex(Scanner.SexEnum.M)
        .lastName("Scanner")
        .firstName("User")
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .birthDate(LocalDate.parse("2000-01-02"))
        .address("Adr 1")
        .email(randomUUID() + "@hei.school");
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
