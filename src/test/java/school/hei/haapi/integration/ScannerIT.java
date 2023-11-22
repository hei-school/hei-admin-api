package school.hei.haapi.integration;

import java.time.Instant;
import java.time.LocalDate;
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
import school.hei.haapi.endpoint.rest.model.ScannerUser;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;

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
  void manager_read_scanner_users_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi usersApi = new UsersApi(manager1Client);

    List<ScannerUser> actual = usersApi.getScannerUsers(1, 20, null, null, null);
    assertEquals(scannerUser(), actual.get(0));
    assertTrue(actual.contains(scannerUser()));
  }

  @Test
  void manager_create_scanner_users_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi usersApi = new UsersApi(manager1Client);
    ScannerUser toCreate = scannerUserToCreate();

    List<ScannerUser> actual = usersApi.createOrUpdateScannerUsers(List.of(toCreate));
    assertEquals(1, actual.size());
    ScannerUser created = actual.get(0);
    assertTrue(isValidUUID(created.getId()));
    toCreate.setId(created.getId());
    assertEquals(created, toCreate);
  }

  public static ScannerUser scannerUser() {
    return new ScannerUser()
        .id("scanner1_id")
        .phone("0340000000")
        .email("adriano.haritiana.123@gmail.com")
        .status(EnableStatus.ENABLED)
        .ref("SCN20001")
        .sex(ScannerUser.SexEnum.M)
        .lastName("Haritiana")
        .firstName("Adriano")
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .birthDate(LocalDate.parse("2000-01-01"))
        .address("Adr 1");
  }

  public static ScannerUser scannerUserToCreate() {
    return new ScannerUser()
        .phone("0320000000")
        .status(EnableStatus.ENABLED)
        .ref("SCN20002")
        .sex(ScannerUser.SexEnum.M)
        .lastName("Scanner")
        .firstName("User")
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .birthDate(LocalDate.parse("2000-01-02"))
        .address("Adr 1")
        .email("test+scanner1@hei.school");
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
