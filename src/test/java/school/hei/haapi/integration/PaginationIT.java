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

  @MockBean private SentryConf sentryConf;

  @MockBean private CognitoComponent cognitoComponentMock;

  @MockBean private EventBridgeClient eventBridgeClientMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  public void setUp() throws ApiException {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
  }

  private void someCreatableStudentList(int nbOfNewStudents) throws ApiException {
    List<Student> newStudents = new ArrayList<>();
    for (int i = 0; i < nbOfNewStudents; i++) {
      newStudents.add(StudentIT.someCreatableStudent());
    }
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    api.createOrUpdateStudents(newStudents);
  }

  @Test
  void student_pages_are_ordered_by_reference() throws ApiException {
    someCreatableStudentList(8);
    int pageSize = 4;
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    UsersApi api = new UsersApi(teacher1Client);

    final List<Student> page1 = api.getStudents(1, pageSize);
    final List<Student> page2 = api.getStudents(2, pageSize);
    final List<Student> page3 = api.getStudents(3, pageSize);
    final List<Student> page4 = api.getStudents(4, pageSize);
    final  List<Student> page100 = api.getStudents(100, pageSize);

    assertEquals(pageSize, page1.size());
    assertEquals(pageSize, page2.size());
    assertEquals(2, page3.size());
    assertEquals(0, page4.size());
    assertEquals(0, page100.size());
    // students are ordered by ref
    assertTrue(isBefore(page1.get(0).getRef(), page1.get(2).getRef()));
    assertTrue(isBefore(page1.get(2).getRef(), page2.get(0).getRef()));
    assertTrue(isBefore(page2.get(0).getRef(), page2.get(2).getRef()));
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
        () -> api.getStudents(0, 20));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"page size must be <500\"}",
        () -> api.getStudents(1, 1000));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
