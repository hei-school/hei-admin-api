package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.conf.TestUtils.student1;
import static school.hei.haapi.integration.conf.TestUtils.student2;
import static school.hei.haapi.model.User.Role.STUDENT;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Testcontainers
@AutoConfigureMockMvc
class UserServiceTest extends FacadeITMockedThirdParties {
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

  @Test
  @Disabled("Affected by other tests, should be re-enabled after tests refactoring")
  void dao_can_handle_null_value_in_params() {
    List<User> students =
        subject.getByCriteria(
            STUDENT, null, null, null, new PageFromOne(1), new BoundedPageSize(15), null, null);
    assertEquals(student1().getId(), students.getFirst().getId());
    assertEquals(student2().getId(), students.get(1).getId());
  }

  @Test
  void disabled_user_enable_ko() {
    User student1 = subject.findById(STUDENT1_ID);
    student1.setStatus(User.Status.DISABLED);
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          student1.setStatus(User.Status.ENABLED);
        });
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }
}
