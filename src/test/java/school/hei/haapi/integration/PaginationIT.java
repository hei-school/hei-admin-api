package school.hei.haapi.integration;

import java.util.ArrayList;
import java.util.List;
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
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PaginationIT.ContextInitializer.class)
@AutoConfigureMockMvc
class PaginationIT {

  @MockBean
  private SentryConf sentryConf;

  @MockBean
  private CognitoComponent cognitoComponentMock;

  @MockBean
  private EventBridgeClient eventBridgeClientMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  public void setUp() throws ApiException {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
  }

  private void createNewStudents(int nbOfNewStudents) throws ApiException {
    List<Student> newStudents = new ArrayList<>();
    for (int i = 0; i < nbOfNewStudents; i++) {
      newStudents.add(StudentIT.aCreatableStudent());
    }
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    api.createOrUpdateStudents(newStudents);
  }

  @Test
  void student_pages_are_ordered_by_reference() throws ApiException {
    createNewStudents(100);
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    UsersApi api = new UsersApi(teacher1Client);
    List<Student> page5 = api.getStudents(5, 5);
    List<Student> page10 = api.getStudents(10, 5);
    List<Student> page100 = api.getStudents(100, 5);

    assertEquals(5, page5.size());
    assertEquals(5, page10.size());
    assertEquals(0, page100.size());
    // students are ordered by ref
    assertTrue(isBefore(page5.get(0).getRef(), page5.get(4).getRef()));
    assertTrue(isBefore(page5.get(4).getRef(), page10.get(0).getRef()));
    assertTrue(isBefore(page10.get(0).getRef(), page10.get(4).getRef()));
  }

  private boolean isBefore(String a, String b) {
    return a.compareTo(b) < 0;
  }

  @Test
  void page_parameters_are_validated() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    UsersApi api = new UsersApi(teacher1Client);
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"page value must be >=1\"}",
        () -> api.getStudents(0, 20)
    );
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"page size must be <500\"}",
        () -> api.getStudents(1, 1000)
    );
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
