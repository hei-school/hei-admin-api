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
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseDirection;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.CourseStatus.LINKED;
import static school.hei.haapi.endpoint.rest.model.CourseStatus.UNLINKED;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.course1;
import static school.hei.haapi.integration.conf.TestUtils.course2;
import static school.hei.haapi.integration.conf.TestUtils.course3;
import static school.hei.haapi.integration.conf.TestUtils.course4;
import static school.hei.haapi.integration.conf.TestUtils.course5;
import static school.hei.haapi.integration.conf.TestUtils.crupdatedCourse1;
import static school.hei.haapi.integration.conf.TestUtils.crupdatedCourse2;
import static school.hei.haapi.integration.conf.TestUtils.isBefore;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.updateStudentCourse;

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
    return TestUtils.anApiClient(token, CourseIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);

    List<Course> actual = api.getCourses(null, null, null, null, null,
        null, null, null, null);

    assertEquals(7, actual.size());
    assertTrue(actual.contains(course1()));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);

    List<Course> actual = api.getCourses(null, null, null, null, null,
        null, null, null, null);

    assertEquals(7, actual.size());
    assertTrue(actual.contains(course2()));
  }

  @Test
  void user_read_by_filter() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<Course> actualByCode = api.getCourses("PROG1", null, null, null, null,
        null, null, null, null);

    List<Course> actualByCode2 = api.getCourses("PROG", null, null, null, null,
        null, null, null, null);

    List<Course> actualByCredits = api.getCourses(null, null, 4, null, null,
        null, null, null, null);

    List<Course> actualByCredits2 = api.getCourses(null, null, 6, null, null,
        null, null, null, null);

    List<Course> actualByLastName = api.getCourses(null, null, null, null, "tEaC",
        null, null, null, null);

    List<Course> actualByCodeAndName = api.getCourses("W", "w", null, null, null,
        null, null, null, null);

    List<Course> actualByCreditsOrderedAsc = api.getCourses(null, null, null, null, null,
        CourseDirection.ASC, null, null, null);

    List<Course> actualByCreditsOrderedDesc = api.getCourses(null, null, null, null, null,
        CourseDirection.DESC, null, null, null);

    assertEquals(1, actualByCode.size());
    assertTrue(actualByCode.contains(course1()));

    assertEquals(2, actualByCode2.size());
    assertTrue(actualByCode2.contains(course1()));
    assertTrue(actualByCode2.contains(course2()));

    assertEquals(1, actualByCredits.size());
    assertTrue(actualByCredits.contains(course3()));

    assertEquals(3, actualByCredits2.size());
    assertTrue(actualByCredits2.contains(course1()));
    assertTrue(actualByCredits2.contains(course2()));
    assertTrue(actualByCredits2.contains(course4()));

    assertEquals(6, actualByLastName.size());
    assertTrue(actualByLastName.contains(course3()));

    assertEquals(2, actualByCodeAndName.size());
    assertTrue(actualByCodeAndName.contains(course3()));
    assertTrue(actualByCodeAndName.contains(course4()));

    assertEquals(7, actualByCreditsOrderedAsc.size());
    assertTrue(isBefore(actualByCreditsOrderedAsc.get(0).getCredits(),
        actualByCreditsOrderedAsc.get(1).getCredits()));
    assertTrue(isBefore(actualByCreditsOrderedAsc.get(1).getCredits(),
        actualByCreditsOrderedAsc.get(2).getCredits()));

    assertTrue(isBefore(actualByCreditsOrderedDesc.get(5).getCredits(),
        actualByCreditsOrderedDesc.get(4).getCredits()));
    assertTrue(isBefore(actualByCreditsOrderedDesc.get(6).getCredits(),
        actualByCreditsOrderedDesc.get(5).getCredits()));

  }

  @Test
  void student_read_own_student_course_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    UsersApi api = new UsersApi(student1Client);

    List<Course> actual1 = api.getStudentCoursesById(STUDENT1_ID, LINKED);
    List<Course> actual2 = api.getStudentCoursesById(STUDENT1_ID, UNLINKED);

    assertEquals(1, actual1.size());
    assertTrue(actual1.contains(course1()));
    assertEquals(new ArrayList<>(), actual2);
    assertEquals(2, actual2.size());
    assertTrue(actual2.contains(course2()));
  }

  @Test
  void student_read_student_course_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    UsersApi api = new UsersApi(student1Client);

    assertThrowsForbiddenException(() -> api.getStudentCoursesById(STUDENT2_ID, LINKED));
    assertThrowsForbiddenException(() -> api.getStudentCoursesById(STUDENT2_ID, UNLINKED));
  }

  @Test
  void user_read_by_student_course() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    UsersApi api = new UsersApi(teacher1Client);

    List<Course> actual1 = api.getStudentCoursesById(STUDENT1_ID, null);
    List<Course> actual2 = api.getStudentCoursesById(STUDENT2_ID, null);

    assertEquals(2, actual1.size());
    assertTrue(actual1.contains(course1()));
    assertEquals(1, actual2.size());
    assertTrue(actual2.contains(course3()));
  }

  @Test
  void user_read_by_student_course_and_status() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Course> actual1 = api.getStudentCoursesById(STUDENT1_ID, LINKED);
    List<Course> actual2 = api.getStudentCoursesById(STUDENT1_ID, UNLINKED);

    assertEquals(1, actual1.size());
    assertTrue(actual1.contains(course1()));
    assertEquals(2, actual2.size());
    assertTrue(actual2.contains(course2()));
  }

  @Test
  void manager_update_student_course() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Course> actual1 = api.updateStudentCourses(STUDENT1_ID, List.of(updateStudentCourse()));
    List<Course> actual2 = api.getStudentCoursesById(STUDENT1_ID, UNLINKED);

    assertTrue(actual1.contains(course3()));
    assertEquals(2, actual2.size());
    assertTrue(actual2.contains(course2()));
  }

  @Test
  void manager_crupdate_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<Course> actual = api.crupdateCourses(List.of(crupdatedCourse1()));

    assertEquals(1, actual.size());
    assertTrue(actual.contains(course5()));
  }

  @Test
  void manager_crupdate_ko() {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Course.MGT1 already exist.\"}",
        () -> api.crupdateCourses(List.of(crupdatedCourse2())));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
