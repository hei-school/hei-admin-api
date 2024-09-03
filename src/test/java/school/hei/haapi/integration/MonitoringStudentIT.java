package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.student1;
import static school.hei.haapi.integration.conf.TestUtils.student2;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.MonitoringApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.UserIdentifier;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MonitoringIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class MonitoringIT extends MockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, MonitoringIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
  }

  @Test
  void monitor_follow_students_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    MonitoringApi api = new MonitoringApi(manager1Client);

    List<Student> studentsLinked =
        api.linkStudentsByMonitorId(MONITOR1_ID, someStudentsIdentifier());

    assertEquals(2, studentsLinked.size());
    assertTrue(studentsLinked.contains(student1()));
    assertTrue(studentsLinked.contains(student2()));
  }

  @Test
  void monitor_follow_students_ko() throws ApiException {
    ApiClient teacher1client = anApiClient(TEACHER1_TOKEN);
    MonitoringApi teacherApi = new MonitoringApi(teacher1client);

    ApiClient monitor1client = anApiClient(MONITOR1_TOKEN);
    MonitoringApi monitorApi = new MonitoringApi(monitor1client);

    ApiClient student1client = anApiClient(STUDENT1_TOKEN);
    MonitoringApi studentApi = new MonitoringApi(student1client);

    assertThrowsForbiddenException(
        () -> teacherApi.linkStudentsByMonitorId(MONITOR1_ID, someStudentsIdentifier()));
    assertThrowsForbiddenException(
        () -> monitorApi.linkStudentsByMonitorId(MONITOR1_ID, someStudentsIdentifier()));
    assertThrowsForbiddenException(
        () -> studentApi.linkStudentsByMonitorId(MONITOR1_ID, someStudentsIdentifier()));
  }

  public static List<UserIdentifier> someStudentsIdentifier() {
    UserIdentifier student1Identifier =
        new UserIdentifier()
            .id("student1_id")
            .firstName("Ryan")
            .lastName("Andria")
            .email("test+ryan@hei.school")
            .ref("STD21001")
            .nic("");

    UserIdentifier student2Identifier =
        new UserIdentifier()
            .id("student2_id")
            .firstName("Two")
            .lastName("Student")
            .email("test+student2@hei.school")
            .ref("STD21002")
            .nic("");

    return Arrays.asList(student1Identifier, student2Identifier);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
