package school.hei.haapi.integration;

import org.junit.jupiter.api.*;
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
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.TeacherIT.*;
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

  public static Course course1() {
    Course course = new Course();
    course.setId("course1_id");
    course.setCode("PROG1");
    course.setName("Algorithms");
    course.setCredits(12);
    course.setTotalHours(22);
    course.setMainTeacher(teacher1());
    return course;
  }
  
  public static UpdateStudentCourse studentCourse1() {
    UpdateStudentCourse updateStudentCourse = new UpdateStudentCourse();
    updateStudentCourse.setCourseId("course1_id");
    updateStudentCourse.setStatus(CourseStatus.LINKED);
    return updateStudentCourse;
  }

  public static UpdateStudentCourse studentCourse2() {
    UpdateStudentCourse updateStudentCourse = new UpdateStudentCourse();
    updateStudentCourse.setCourseId("course2_id");
    updateStudentCourse.setStatus(CourseStatus.LINKED);
    return updateStudentCourse;
  }

  public static Course course2() {
    Course course = new Course();
    course.setId("course2_id");
    course.setCode("WEB1");
    course.setName("Web interface locally interactive");
    course.setCredits(12);
    course.setTotalHours(20);
    course.setMainTeacher(teacher2());
    return course;
  }

  public static CrupdateCourse toCreateSuccess() {
    CrupdateCourse toCreate = new CrupdateCourse();
    toCreate.setId(null);
    toCreate.setCode("PROG2P1");
    toCreate.setName("Object Oriented Programming");
    toCreate.setCredits(8);
    toCreate.setTotalHours(15);
    toCreate.setMainTeacherId(teacher3().getId());
    return toCreate;
  }

  public static CrupdateCourse toCreateWithSomeNullValues() {
    CrupdateCourse toCreate = new CrupdateCourse();
    toCreate.setId(null);
    toCreate.setCode("PROG3P1");
    toCreate.setName(null);
    toCreate.setCredits(12);
    toCreate.setTotalHours(22);
    toCreate.setMainTeacherId(null);
    return toCreate;
  }

  public static CrupdateCourse toUpdateCourse() {
    return new CrupdateCourse()
        .id(course2().getId())
        .name(course2().getName())
        .code(course2().getCode())
        .credits(course2().getCredits())
        .totalHours(course2().getTotalHours())
        .mainTeacherId(teacher3().getId());
  }

  public static CrupdateCourse toCreateWithDuplicatedCode() {
    CrupdateCourse toCreate = new CrupdateCourse();
    toCreate.setCode("PROG3P1");
    toCreate.setName("Complexity Analysis");
    toCreate.setCredits(8);
    toCreate.setTotalHours(15);
    toCreate.setMainTeacherId(null);
    return toCreate;
  }

  public static Course CreatedCourse1() {
    Course createdCourse1 = new Course();
    createdCourse1.setName(toCreateSuccess().getName());
    createdCourse1.setCode(toCreateSuccess().getCode());
    createdCourse1.setCredits(toCreateSuccess().getCredits());
    createdCourse1.setTotalHours(toCreateSuccess().getTotalHours());
    createdCourse1.setMainTeacher(teacher3());
    return createdCourse1;
  }

  public static Course CreatedCourse2() {
    Course createdCourse2 = new Course();
    createdCourse2.setName(null);
    createdCourse2.setCode(toCreateWithSomeNullValues().getCode());
    createdCourse2.setCredits(toCreateWithSomeNullValues().getCredits());
    createdCourse2.setTotalHours(toCreateWithSomeNullValues().getTotalHours());
    createdCourse2.setMainTeacher(null);
    return createdCourse2;
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponent);
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(apiClient);

    List<Course> actual = api.getCourses(1, 15);

    assertEquals(5, actual.size());
    assertTrue(actual.containsAll(List.of(course1(), course2())));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
