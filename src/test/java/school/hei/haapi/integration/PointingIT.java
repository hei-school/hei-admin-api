package school.hei.haapi.integration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.PointingTestUtils.pointing1Ok;
import static school.hei.haapi.integration.conf.PointingTestUtils.pointing2Ok;
import static school.hei.haapi.integration.conf.PointingTestUtils.pointing3Late;
import static school.hei.haapi.integration.conf.PointingTestUtils.pointing4Late;
import static school.hei.haapi.integration.conf.PointingTestUtils.pointing5Late;
import static school.hei.haapi.integration.conf.PointingTestUtils.pointing6Missing;
import static school.hei.haapi.integration.conf.PointingTestUtils.pointing7Missing;
import static school.hei.haapi.integration.conf.PointingTestUtils.student1;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.course1;
import static school.hei.haapi.integration.conf.TestUtils.course2;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.CheckInApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Pointing;
import school.hei.haapi.endpoint.rest.model.PointingCourseType;
import school.hei.haapi.endpoint.rest.model.PointingType;
import school.hei.haapi.endpoint.rest.model.StudentMissing;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PointingIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class PointingIT {
  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void teacher_read_pointing_from_a_course_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    CheckInApi api = new CheckInApi(teacher1Client);

    List<StudentMissing> actual = api.getCoursesPointing(course1().getId(), 1, 10, null, null, null, null,
        null);
    List<StudentMissing> data = new ArrayList<>();
    data.add(pointing1Ok());
    data.add(pointing2Ok());
    data.add(pointing5Late());
    data.add(pointing6Missing());
    data.add(pointing7Missing());

    assertEquals(5, actual.size());
    assertTrue(actual.containsAll(data));
  }

  @Test
  void manager_read_pointing_from_a_course_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    CheckInApi api = new CheckInApi(manager1Client);

    List<StudentMissing> actual = api.getCoursesPointing(course1().getId(), 1, 10, null, null, null, null,
        null);
    List<StudentMissing> data = new ArrayList<>();
    data.add(pointing1Ok());
    data.add(pointing2Ok());
    data.add(pointing5Late());
    data.add(pointing6Missing());
    data.add(pointing7Missing());

    assertEquals(5, actual.size());
    assertTrue(actual.containsAll(data));
  }

  @Test
  void manager_read_pointing_from_a_course_with_criteria() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    CheckInApi api = new CheckInApi(manager1Client);

    List<StudentMissing> actualWithLate1 = api.getCoursesPointing(course1().getId(), 1, 10, null, null, null, null, PointingCourseType.LATE);
    assertTrue(actualWithLate1.containsAll(List.of(pointing3Late(), pointing5Late())));

    List<StudentMissing> actualWithLate2 = api.getCoursesPointing(course2().getId(), 1, 10, null, null, null, null, PointingCourseType.LATE);
    assertTrue(actualWithLate2.contains(pointing4Late()));

    List<StudentMissing> actualWithMissing = api.getCoursesPointing(course1().getId(), 1, 10, null, null, null, null, PointingCourseType.MISSING);
    assertTrue(actualWithMissing.containsAll(List.of(pointing6Missing(), pointing7Missing())));

    List<StudentMissing> actualWithFirstnameCriteria = api.getCoursesPointing(course1().getId(), 1, 10, "Two", null, null, null, PointingCourseType.LATE);
    assertTrue(actualWithFirstnameCriteria.contains(pointing3Late()));

    List<StudentMissing> actualWithLastnameCriteria = api.getCoursesPointing(course1().getId(), 1, 10, null, "Student", null, null, PointingCourseType.LATE);
    assertTrue(actualWithLastnameCriteria.contains(pointing3Late()));

    List<StudentMissing> actualWithStudentNameCriteria = api.getCoursesPointing(course1().getId(), 1, 10, "Two", "Student", null, null, PointingCourseType.LATE);
    assertTrue(actualWithStudentNameCriteria.contains(pointing3Late()));

    List<StudentMissing> actualWithBeginDate = api.getCoursesPointing(course1().getId(), 1, 10, null, null,
        Instant.parse("2021-12-08T09:00:24.00Z"), null, PointingCourseType.LATE);
    assertTrue(actualWithBeginDate.contains(pointing5Late()));

    List<StudentMissing> actualBetweenASpecificDate = api.getCoursesPointing(course1().getId(), 1, 10, null, null,
        Instant.parse("2021-12-08T09:00:24.00Z"), Instant.parse("2022-12-08T09:00:24.00Z"), PointingCourseType.LATE);
    assertTrue(actualBetweenASpecificDate.contains(pointing5Late()));
  }

  @Test
  void manager_create_pointing_ok() throws ApiException {
    ApiClient manager1CLient = anApiClient(MANAGER1_TOKEN);
    CheckInApi api = new CheckInApi(manager1CLient);

    Pointing actual = api.createPointing(student1().getId(), "Andraharo", PointingType.ENTER);
    Pointing expected = new Pointing()
        .id(pointing1Ok().getId())
        .student(student1())
        .place("Andraharo")
        .datetime(Instant.parse("2021-11-08T08:25:24.00Z"));
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
