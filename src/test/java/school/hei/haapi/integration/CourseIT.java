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
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

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
    course.setRef("PROG1");
    course.setName("Algorithmics");
    course.setCredits(2);
    course.setTotalHours(22);
    return course;
  }


  public static Course course2() {
    Course course = new Course();
    course.setId("course2_id");
    course.setRef("SYS1");
    course.setName("Operating system");
    course.setCredits(4);
    course.setTotalHours(44);
    return course;
  }

  public static Course someCreatableCourse1() {
    Course course = new Course();
    course.setId("course3_id");
    course.setRef("SYS2");
    course.setName("Cloud computing");
    course.setCredits(2);
    course.setTotalHours(22);
    return course;
  }
  public static Course someCreatableCourse2() {
    Course course = new Course();
    course.setId("course4_id");
    course.setRef("PROG2");
    course.setName("API programming");
    course.setCredits(3);
    course.setTotalHours(33);
    return course;
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    TeachingApi api = new TeachingApi(student1Client);
    List<Course> courses = api.getCourses();

    assertTrue(courses.contains(course1()));
    assertTrue(courses.contains(course2()));
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdateCourses(someCreatableCourse1()));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(TEACHER1_TOKEN);

    TeachingApi api = new TeachingApi(student1Client);
    List<Course> courses = api.getCourses();

    assertTrue(courses.contains(course1()));
    assertTrue(courses.contains(course2()));
  }
  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    TeachingApi api = new TeachingApi(teacher1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdateCourses(someCreatableCourse1()));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(MANAGER1_TOKEN);

    TeachingApi api = new TeachingApi(student1Client);
    List<Course> courses = api.getCourses();

    assertTrue(courses.contains(course1()));
    assertTrue(courses.contains(course2()));
  }

  @Test
  void manager_write_create_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    Course toCreate1 = someCreatableCourse1();


    TeachingApi api = new TeachingApi(manager1Client);
    Course created = api.createOrUpdateCourses(toCreate1);

    assertTrue(isValidUUID(created.getId()));
    List<Course> actual = api.getCourses();
    assertTrue(actual.contains(toCreate1));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
