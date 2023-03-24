package school.hei.haapi.integration;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.service.UserService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER2_ID;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
@Slf4j
class CourseIT {

  @MockBean
  private SentryConf sentryConf;

  @MockBean
  private CognitoComponent cognitoComponentMock;

  @MockBean
  private EventBridgeClient eventBridgeClientMock;

  @Autowired
  private UserService userService;

  @Autowired
  private UserMapper userMapper;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  Course course1() {
    return new Course()
        .id("course1_id")
        .code("PROG1")
        .name("Algorithme et structure de données")
        .credits(10)
        .totalHours(200)
        .mainTeacher(new Teacher()
            .id("teacher4_id")
            .status(EnableStatus.ENABLED)
            .ref("TCR21004")
            .entranceDatetime(Instant.parse("2021-10-09T08:28:24.00Z"))
            .sex(Teacher.SexEnum.M)
            .address("Adr 4")
            .birthDate(LocalDate.parse("1990-01-02"))
            .lastName("Ramarozaka")
            .firstName("Tokimahery")
            .phone("0322411126")
            .email("test+tokimahery@hei.school"));
  }

  Course course2() {
    return new Course()
        .id("course2_id")
        .code("PROG3")
        .name("POO avancée")
        .credits(10)
        .totalHours(200)
        .mainTeacher(new Teacher()
            .id("teacher5_id")
            .status(EnableStatus.ENABLED)
            .ref("TCR21005")
            .entranceDatetime(Instant.parse("2021-10-09T08:28:24.00Z"))
            .sex(Teacher.SexEnum.M)
            .address("Adr 4")
            .birthDate(LocalDate.parse("1990-01-02"))
            .lastName("Andriamahery")
            .firstName("Ryan")
            .phone("0322411126")
            .email("test+teacher5@hei.school"));
  }

  Course course3() {
    return new Course()
        .id("course3_id")
        .code("WEB1")
        .name("Interface homme machine")
        .credits(8)
        .totalHours(150)
        .mainTeacher(new Teacher()
            .id("teacher4_id")
            .status(EnableStatus.ENABLED)
            .ref("TCR21004")
            .entranceDatetime(Instant.parse("2021-10-09T08:28:24.00Z"))
            .sex(Teacher.SexEnum.M)
            .address("Adr 4")
            .birthDate(LocalDate.parse("1990-01-02"))
            .lastName("Ramarozaka")
            .firstName("Tokimahery")
            .phone("0322411126")
            .email("test+tokimahery@hei.school"));
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
  }

  @Test
  void student_get_courses_without_filter_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);

    List<Course> courses = api.getCourses(1, 15, null, null, null, null, null);

    assertTrue(courses.contains(course1()));
    assertTrue(courses.contains(course2()));
    assertTrue(courses.contains(course3()));
  }

  @Test
  void student_get_courses_filter_by_teacher_first_name_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);

    List<Course> course1 = api.getCourses(1, 15, null, null, null, "Tokimahery", null);
    List<Course> course2 = api.getCourses(1, 15, null, null, null, "Ryan", null);

    assertTrue(course1.contains(course1()));
    assertTrue(course1.contains(course3()));
    assertTrue(course2.contains(course2()));
  }

  @Test
  void student_get_courses_filter_by_teacher_partial_first_name_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);

    List<Course> courses = api.getCourses(1, 15, null, null, null, "Tok", null);

    assertTrue(courses.contains(course1()));
    assertTrue(courses.contains(course3()));
    assertFalse(courses.contains(course2()));
  }

  @Test
  void student_get_courses_filter_by_teacher_first_name_and_last_name() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);

    List<Course> courses = api.getCourses(1, 15, null, null, null, "MahEry", "mahery");

    courses.contains(course1());
    courses.contains(course2());
    courses.contains(course3());
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
