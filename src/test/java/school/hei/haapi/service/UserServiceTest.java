package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = UserServiceTest.ContextInitializer.class)
@AutoConfigureMockMvc
class UserServiceTest extends MockedThirdParties {
  @Autowired private UserService subject;
  @MockBean private EventBridgeClient eventBridgeClientMock;

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
    setUpEventBridge(eventBridgeClientMock);
  }

  @Test
  void monitors_is_retrieve_by_student_id_ok() {
    String expectedMonitorId = "monitor1_id";
    List<String> actualMonitorsId =
        subject.findMonitorsByStudentId("student1_id").stream().map(User::getId).toList();

    assertEquals(1, actualMonitorsId.size());
    assertTrue(actualMonitorsId.contains(expectedMonitorId));
  }

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, UserServiceTest.ContextInitializer.SERVER_PORT);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
