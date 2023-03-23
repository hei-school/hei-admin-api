package school.hei.haapi.integration;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.Direction.ASC;
import static school.hei.haapi.endpoint.rest.model.Direction.DESC;
import static school.hei.haapi.integration.TeacherIT.teacher1;
import static school.hei.haapi.integration.TeacherIT.teacher2;
import static school.hei.haapi.integration.TeacherIT.teacher3;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE3_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
class CourseIT {

  @MockBean
  private SentryConf sentryConf;

  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  private static Course course1() {
    return new Course()
        .id(COURSE1_ID)
        .code("PROG1")
        .name("Algorithmique")
        .credits(6)
        .totalHours(11)
        .mainTeacher(teacher1());
  }

  private static Course course2() {
    return new Course()
        .id(COURSE2_ID)
        .code("PROG3")
        .name("P.O.O avancée")
        .credits(6)
        .totalHours(12)
        .mainTeacher(teacher3());
  }

  private static Course course3() {
    return new Course()
        .id(COURSE3_ID)
        .code("WEB1")
        .name("Interface web")
        .credits(4)
        .totalHours(13)
        .mainTeacher(teacher2());
  }


  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void user_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi student1Api = new TeachingApi(student1Client);
    TeachingApi teacher1Api = new TeachingApi(teacher1Client);
    TeachingApi manager1Api = new TeachingApi(manager1Client);
    List<Course> expectedCourses = List.of(course1(), course2(), course3());

    List<Course> actualFromStudent =
        student1Api.getCourses(1, 100, null, null, null, null, null, null, null);
    List<Course> actualFromTeacher =
        teacher1Api.getCourses(1, 100, null, null, null, null, null, null, null);
    List<Course> actualFromManager =
        manager1Api.getCourses(1, 100, null, null, null, null, null, null, null);

    assertTrue(actualFromStudent.containsAll(expectedCourses));
    assertTrue(actualFromTeacher.containsAll(expectedCourses));
    assertTrue(actualFromManager.containsAll(expectedCourses));
  }

  @Test
  void read_filtered_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi student1Api = new TeachingApi(student1Client);
    //get a specific course
    List<Course> filteredByAllCriterias =
        student1Api.getCourses(1, 1, course1().getCode(), course1().getName(),
            course1().getCredits(), course1().getMainTeacher().getFirstName(),
            course1().getMainTeacher().getLastName(), null, null);
    //teachers have "tea" in common
    List<Course> filteredByLastNameContainingIgnoreCase =
        student1Api.getCourses(1, 100, null, null, null, null, "tEa", null, null);

    assertTrue(filteredByAllCriterias.contains(course1()));
    assertEquals(1, filteredByAllCriterias.size());
    assertTrue(filteredByLastNameContainingIgnoreCase.containsAll(
        List.of(course1(), course2(), course3())));
    assertEquals(3, filteredByLastNameContainingIgnoreCase.size());
  }

  @Test
  void read_ordered_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi student1Api = new TeachingApi(student1Client);

    List<Course> orderedByCredits =
        student1Api.getCourses(1, 100, null, null, null, null, null, null, ASC);
    List<Course> orderedByCreditsAndCode =
        student1Api.getCourses(1, 100, null, null, null, null, null, DESC, DESC);

    assertEquals(course3(), orderedByCredits.get(0));
    assertEquals(course1(), orderedByCredits.get(1));
    assertEquals(course2(), orderedByCredits.get(2));
    assertEquals(course2(), orderedByCreditsAndCode.get(0));
    assertEquals(course1(), orderedByCreditsAndCode.get(1));
    assertEquals(course3(), orderedByCreditsAndCode.get(2));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
