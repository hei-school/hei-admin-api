package school.hei.haapi.integration;

import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.createAttendanceMovement;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.student1;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.AttendanceApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.AttendanceMovementType;
import school.hei.haapi.endpoint.rest.model.PlaceEnum;
import school.hei.haapi.endpoint.rest.model.StudentAttendance;
import school.hei.haapi.endpoint.rest.model.StudentAttendanceMovement;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = AttendanceIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class AttendanceIT {
  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private CognitoComponent cognitoComponent;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponent);
  }

  @Test
  void manager_create_attendance_movement_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    AttendanceApi api = new AttendanceApi(manager1Client);

    StudentAttendanceMovement actual = api.createAttendanceMovement(createAttendanceMovement());
    StudentAttendanceMovement expected = new StudentAttendanceMovement()
        .id("attendance1_id")
        .place(PlaceEnum.ANDRAHARO)
        .createdAt(Instant.parse("2021-11-08T07:30:00.00Z"))
        .student(student1())
        .attendanceMovementType(AttendanceMovementType.IN);

    assertTrue(actual.equals(expected));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
