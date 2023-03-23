package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.TeacherIT.teacher1;
import static school.hei.haapi.integration.TeacherIT.teacher2;
import static school.hei.haapi.integration.TeacherIT.teacher3;
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

  private static ApiClient anApiClient() {
    return TestUtils.anApiClient(TestUtils.BAD_TOKEN, CourseIT.ContextInitializer.SERVER_PORT);
  }

  static Course course1() {
    Course course = new Course();
    course.setId("course_ID");
    course.setCode("PROG1");
    course.setName("JS");
    course.setCredits(6);
    course.setTotalHours(20);
    course.setMainTeacher(teacher1());
    return course;
  }

  static Course course2() {
    Course course = new Course();
    course.setId("course2_ID");
    course.setCode("WEB1");
    course.setName("HTML Basic");
    course.setCredits(4);
    course.setTotalHours(19);
    course.setMainTeacher(teacher2());
    return course;
  }

  static Course course3() {
    Course course = new Course();
    course.setId("course3_ID");
    course.setCode("PROG3");
    course.setName("JAVA");
    course.setCredits(6);
    course.setTotalHours(28);
    course.setMainTeacher(teacher3());
    return course;
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
