package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Order;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.TeacherIT.teacher1;
import static school.hei.haapi.integration.TeacherIT.teacher2;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class CourseIT {
  @MockBean
  private SentryConf sentryConf;

  @MockBean
  private CognitoComponent cognitoComponent;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  public static school.hei.haapi.endpoint.rest.model.Course course1() {
    school.hei.haapi.endpoint.rest.model.Course course = new school.hei.haapi.endpoint.rest.model.Course();
    course.setId("course1_id");
    course.setCode("PROG1");
    course.setName("Algorithms");
    course.setCredits(12);
    course.setTotalHours(22);
    course.setMainTeacher(teacher1());
    return course;
  }

  public static school.hei.haapi.endpoint.rest.model.Course course2() {
    school.hei.haapi.endpoint.rest.model.Course course = new school.hei.haapi.endpoint.rest.model.Course();
    course.setId("course2_id");
    course.setCode("WEB1");
    course.setName("Web interface locally interactive");
    course.setCredits(12);
    course.setTotalHours(20);
    course.setMainTeacher(teacher2());
    return course;
  }

  public static school.hei.haapi.endpoint.rest.model.Course course3() {
    school.hei.haapi.endpoint.rest.model.Course course = new school.hei.haapi.endpoint.rest.model.Course();
    course.setId("course3_id");
    course.setCode("WEB2");
    course.setName("UI/UX");
    course.setCredits(10);
    course.setTotalHours(20);
    course.setMainTeacher(teacher2());
    return course;
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponent);
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(apiClient);

    List<school.hei.haapi.endpoint.rest.model.Course> actual = api.getCourses(1, 15, null, null, null,
            null, null, null, null);

    assertEquals(3, actual.size());
    assertTrue(actual.contains(course3()));
  }
  @Test
  void manager_read_ok() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(apiClient);

    List<school.hei.haapi.endpoint.rest.model.Course> actual = api.getCourses(1, 15, null, null, null,
            null, null, null, null);

    assertEquals(3, actual.size());
    assertTrue(actual.contains(course1()));
    assertTrue(actual.contains(course2()));
    assertTrue(actual.contains(course3()));
  }

  @Test
  void student_read_in_default_order_ok() throws ApiException {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(apiClient);

    List<school.hei.haapi.endpoint.rest.model.Course> actual = api.getCourses(1, 15,
            null, null, null, null, null,
            Order.ASC, Order.ASC);

    assertEquals(3, actual.size());
    assertTrue(actual.contains(course3()));
  }

  @Test
  void teacher_read_with_invalid_page_ko() {
    ApiClient apiClient = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(apiClient);

    assertThrowsApiException(
            "{\"type\":\"400 BAD_REQUEST\",\"message\":\"page value must be >=1\"}",
            () -> api.getCourses((-1), 15,
                    null, null, null, null, null, null, null));
  }
  @Test
  void teacher_read_ko() {
    ApiClient apiClient = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(apiClient);

    assertThrowsApiException(
            "{\"type\":\"400 BAD_REQUEST\",\"message\":\"page value must be >=1\"}",
            () -> api.getCourses(-1, 15, null, null, null,
                    null, null, null, null));
  }


  @Test
  void student_read_sorted_by_credits_desc() throws ApiException {
      ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
      TeachingApi api = new TeachingApi(apiClient);

      List<school.hei.haapi.endpoint.rest.model.Course> actual = api.getCourses(1, 15, null, null, null,
              null, null, Order.DESC, null);

      assertEquals(3, actual.size());
      assertEquals(course3(), actual.get(0));
      assertEquals(course1(), actual.get(1));
      assertEquals(course2(), actual.get(2));
    }
  @Test
  void student_read_sorted_by_code_asc() throws ApiException {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(apiClient);

    List<school.hei.haapi.endpoint.rest.model.Course> actual = api.getCourses(1, 15, null, null, null,
            null, null, null, Order.ASC);

    assertEquals(3, actual.size());
    assertEquals(course1(), actual.get(0));
    assertEquals(course2(), actual.get(1));
    assertEquals(course3(), actual.get(2));
  }

  @Test
  void student_read_ko_invalid_credentials() {
    ApiClient apiClient = anApiClient(BAD_TOKEN);
    TeachingApi api = new TeachingApi(apiClient);

    assertThrowsApiException(
            "{\"type\":\"401 UNAUTHORIZED\",\"message\":\"Full authentication is required to access this resource\"}",
            () -> api.getCourses(1, 15, null, null, null,
                    null, null, null, null));
  }

  @Test
  void student_read_ko_invalid_page() {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(apiClient);

    assertThrowsApiException(
            "{\"type\":\"400 BAD_REQUEST\",\"message\":\"page value must be >=1\"}",
            () -> api.getCourses(-1, 15, null, null, null,
                    null, null, null, null));
  }

  @Test
  void student_read_ko_invalid_page_size() {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(apiClient);

    assertThrowsApiException(
            "{\"type\":\"400 BAD_REQUEST\",\"message\":\"page size value must be between 5 and 20\"}",
            () -> api.getCourses(1, 4, null, null, null,
                    null, null, null, null));
  }

  @Test
  void student_read_ko_no_results() {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(apiClient);

    assertThrowsApiException(
            "{\"type\":\"404 NOT_FOUND\",\"message\":\"No courses found\"}",
            () -> api.getCourses(2, 15, null, null, null,
                    null, null, null, null));
  }

  @Test
  void student_read_ko_invalid_teacher_name() {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(apiClient);

    assertThrowsApiException(
            "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Invalid teacher name\"}",
            () -> api.getCourses(1, 15, null, null, null,
                    "123", "Smith", null, null));
  }
  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
