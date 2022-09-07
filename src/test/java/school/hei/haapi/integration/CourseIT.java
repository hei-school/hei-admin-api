package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
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

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class CourseIT {

  @MockBean
  private SentryConf sentryConf;

  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  public static Course course1() {
    Course course = new Course();
    course.setId("course1_id");
    course.setName("Name of course one");
    course.setRef("CRS21001");
    course.setCredits(10);
    course.setTotalHours(40);
    return course;
  }

  public static Course course2() {
    Course course = new Course();
    course.setId("course2_id");
    course.setName("Name of course two");
    course.setRef("CRS21002");
    course.setCredits(5);
    course.setTotalHours(45);
    return course;
  }

  public static Course aCreatableCourse() {
    Course course = new Course();
    course.setName("Some name");
    course.setRef("CRS21-" + randomUUID());
    course.setCredits(10);
    course.setTotalHours(45);
    return course;
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void badtoken_read_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    TeachingApi api = new TeachingApi(anonymousClient);
    assertThrowsForbiddenException(api::getCourses);
  }

  @Test
  void badtoken_write_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    TeachingApi api = new TeachingApi(anonymousClient);
    assertThrowsForbiddenException(() -> api.createOrUpdateCourses(new Course()));
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    TeachingApi api = new TeachingApi(student1Client);
    Course actual1 = api.getCourseById(COURSE1_ID);
    List<Course> actualCourses = api.getCourses();

    assertEquals(course1(), actual1);
    assertTrue(actualCourses.contains(course1()));
    assertTrue(actualCourses.contains(course2()));
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdateCourses(course1()));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    TeachingApi api = new TeachingApi(teacher1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdateCourses(course1()));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(MANAGER1_TOKEN);

    TeachingApi api = new TeachingApi(student1Client);
    Course actual1 = api.getCourseById(COURSE1_ID);
    List<Course> actualCourses = api.getCourses();

    assertEquals(course1(), actual1);
    assertTrue(actualCourses.contains(course1()));
    assertTrue(actualCourses.contains(course2()));
  }
  @Test
  void manager_write_create_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    Course toCreate1 = aCreatableCourse();

    TeachingApi api = new TeachingApi(manager1Client);
    Course created = api.createOrUpdateCourses(toCreate1);
    toCreate1.setId(created.getId());

    assertEquals(toCreate1, created);
    assertTrue(isValidUUID(created.getId()));
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    Course toUpdate = api.createOrUpdateCourses(
        aCreatableCourse());
    toUpdate.setName("A new name zero");
    Course updated = api.createOrUpdateCourses(toUpdate);

    assertEquals(toUpdate, updated);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
