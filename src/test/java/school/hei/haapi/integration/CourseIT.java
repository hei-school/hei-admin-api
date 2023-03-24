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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.SortDirection.ASC;
import static school.hei.haapi.integration.TeacherIT.teacher1;
import static school.hei.haapi.integration.TeacherIT.teacher2;
import static school.hei.haapi.integration.TeacherIT.teacher3;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE3_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
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
    return TestUtils.anApiClient(token, CourseIT.ContextInitializer.SERVER_PORT);
  }

  static Course course1() {
    Course course = new Course();
    course.setId(COURSE1_ID);
    course.setCode("PROG1");
    course.setName("Algorithmique");
    course.setCredits(6);
    course.setTotalHours(20);
    course.setMainTeacher(teacher1());
    return course;
  }

  static Course course2() {
    Course course = new Course();
    course.setId(COURSE2_ID);
    course.setCode("WEB1");
    course.setName("Interface web");
    course.setCredits(4);
    course.setTotalHours(19);
    course.setMainTeacher(teacher2());
    return course;
  }

  static Course course3() {
    Course course = new Course();
    course.setId(COURSE3_ID);
    course.setCode("PROG3");
    course.setName("P.O.O avancée");
    course.setCredits(6);
    course.setTotalHours(28);
    course.setMainTeacher(teacher3());
    return course;
  }

  static List<Course> getCoursesWithNoCriteria(TeachingApi api)
      throws ApiException {
    return api.getCourses(1, 25, null, null, null, null, null, null, null);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void badtoken_read_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    TeachingApi api = new TeachingApi(anonymousClient);
    assertThrowsForbiddenException(() -> getCoursesWithNoCriteria(api));
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);

    List<Course> actual = getCoursesWithNoCriteria(api);

    assertTrue(actual.contains(course1()));
    assertTrue(actual.contains(course2()));
    assertTrue(actual.contains(course3()));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<Course> actual = getCoursesWithNoCriteria(api);

    assertTrue(actual.contains(course1()));
    assertTrue(actual.contains(course2()));
    assertTrue(actual.contains(course3()));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);

    List<Course> actual = getCoursesWithNoCriteria(api);

    assertTrue(actual.contains(course1()));
    assertTrue(actual.contains(course2()));
    assertTrue(actual.contains(course3()));
  }

  @Test
  void read_and_filter_by_teacher_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);

    List<Course> byFirstName = api.getCourses(1, 25, null, null, null, "T", null, null, null);
    List<Course> byLastName = api.getCourses(1, 25, null, null, null, null, "teach", null, null);

    // teacher2_id and teacher3_id has 'T' on their first_name
    assertFalse(byFirstName.contains(course1()));
    assertTrue(byFirstName.contains(course2()));
    assertTrue(byFirstName.contains(course3()));

    // all teacher's last_name contains `teach`
    assertTrue(byLastName.contains(course1()));
    assertTrue(byLastName.contains(course2()));
    assertTrue(byLastName.contains(course3()));
  }

  @Test
  void read_and_filter_by_its_metadata_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);

    List<Course> actual = api.getCourses(1, 25, "PROG", null, 6, null, null, null, null);

    assertTrue(actual.contains(course1()));
    assertFalse(actual.contains(course2()));
    assertTrue(actual.contains(course3()));
  }

  @Test
  void read_and_sort_by_order_field_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);

    List<Course> actual =
        api.getCourses(1, 25, null, null, null, null, null, ASC, ASC);

    assertEquals(actual.get(0), course1());
    assertEquals(actual.get(1), course3());
    assertEquals(actual.get(2), course2());
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}