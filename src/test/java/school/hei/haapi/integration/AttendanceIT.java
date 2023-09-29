package school.hei.haapi.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.StudentIT.student2;
import static school.hei.haapi.integration.StudentIT.student3;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.course1;
import static school.hei.haapi.integration.conf.TestUtils.course2;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.AttendanceApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.AttendanceMovementType;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.CourseSession;
import school.hei.haapi.endpoint.rest.model.CreateAttendanceMovement;
import school.hei.haapi.endpoint.rest.model.PlaceEnum;
import school.hei.haapi.endpoint.rest.model.StudentAttendance;
import school.hei.haapi.endpoint.rest.model.StudentAttendanceMovement;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = AttendanceIT.ContextInitializer.class)
@AutoConfigureMockMvc
class AttendanceIT {
  private final static Instant DEFAULT_FROM = Instant.parse("2021-08-07T07:30:00.00Z");
  private final static Instant DEFAULT_TO = Instant.parse("2021-11-09T07:30:00.00Z");
  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private CognitoComponent cognitoComponent;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponent);
  }

  @Test
  void manager_create_attendance_movement_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    AttendanceApi api = new AttendanceApi(manager1Client);

    List<StudentAttendanceMovement> actual = api.createAttendanceMovement(List.of(createAttendanceMovement()));
    StudentAttendanceMovement expected = new StudentAttendanceMovement()
        .id("attendance1_id")
        .place(PlaceEnum.ANDRAHARO)
        .createdAt(Instant.parse("2021-11-08T07:30:00.00Z"))
        .student(student1())
        .attendanceMovementType(AttendanceMovementType.IN);

    assertEquals(expected.getCreatedAt(), actual.get(0).getCreatedAt());
    assertEquals(expected.getAttendanceMovementType(), actual.get(0).getAttendanceMovementType());
    assertEquals(expected.getPlace(), actual.get(0).getPlace());
    assertEquals(expected.getStudent(), actual.get(0).getStudent());
  }

  @Test
  void manager_create_attendance_with_no_student_id() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    AttendanceApi api = new AttendanceApi(manager1Client);

    assertThrowsApiException("{\"type\":\"404 NOT_FOUND\",\"message\":\"the student with #student_id_ko doesn't exist\"}",
        () -> api.createAttendanceMovement(List.of(createAttendanceMovementKo())));
  }

  public static CourseSession courseSession1() {
    return new CourseSession()
        .id("course_session1_id")
        .course(course1())
        .begin(Instant.parse("2021-11-08T08:00:00.00Z"))
        .end(Instant.parse("2021-11-08T12:00:00.00Z"));
  }

  public static CourseSession courseSession2() {
    return new CourseSession()
        .id("course_session2_id")
        .course(course2())
        .begin(Instant.parse("2021-08-08T15:00:00.00Z"))
        .end(Instant.parse("2021-08-08T17:00:00.00Z"));
  }

  public static StudentAttendance attendance1Ok() {
    return new StudentAttendance()
        .id("attendance1_id")
        .lateOf(0)
        .student(student1())
        .place(PlaceEnum.ANDRAHARO)
        .isLate(false)
        .courseSession(courseSession1())
        .createdAt(Instant.parse("2021-11-08T07:30:00.00Z"));
  }

  public static StudentAttendance attendance2Ok() {
    return new StudentAttendance()
        .id("attendance2_id")
        .student(student1())
        .lateOf(0)
        .place(PlaceEnum.ANDRAHARO)
        .isLate(false)
        .courseSession(courseSession2())
        .createdAt(Instant.parse("2021-08-08T14:15:00.00Z"));
  }

  public static StudentAttendance attendance3Late() {
    return new StudentAttendance()
        .id("attendance3_id")
        .place(PlaceEnum.IVANDRY)
        .isLate(true)
        .lateOf(35)
        .courseSession(courseSession1())
        .student(student2())
        .createdAt(Instant.parse("2021-11-08T08:35:00.00Z"));
  }

  public static StudentAttendance attendance4Late() {
    return new StudentAttendance()
        .id("attendance4_id")
        .isLate(true)
        .lateOf(15)
        .place(PlaceEnum.ANDRAHARO)
        .courseSession(courseSession2())
        .student(student2())
        .createdAt(Instant.parse("2021-08-08T15:15:00.00Z"));
  }

  public static StudentAttendance attendance5Missing() {
    return new StudentAttendance()
        .id("attendance5_id")
        .lateOf(0)
        .isLate(false)
        .student(student3())
        .courseSession(courseSession1())
        .createdAt(null);
  }

  public static StudentAttendance attendance6Missing() {
    return new StudentAttendance()
        .id("attendance6_id")
        .lateOf(0)
        .isLate(false)
        .student(student3())
        .courseSession(courseSession2())
        .createdAt(null);
  }

  public static StudentAttendance attendance7Out() {
    return new StudentAttendance()
        .id("attendance7_id")
        .createdAt(Instant.parse("2021-11-08T012:30:00.00Z"))
        .isLate(false)
        .student(student1())
        .courseSession(courseSession1());
  }

  public static CreateAttendanceMovement createAttendanceMovement() {
    return new CreateAttendanceMovement()
        .place(PlaceEnum.ANDRAHARO)
        .attendanceMovementType(AttendanceMovementType.IN)
        .studentId("student1_id")
        .createdAt(Instant.parse("2021-11-08T07:30:00.00Z"));
  }

  public static CreateAttendanceMovement createAttendanceMovementKo() {
    return new CreateAttendanceMovement()
        .place(PlaceEnum.ANDRAHARO)
        .attendanceMovementType(AttendanceMovementType.IN)
        .studentId("student_id_ko")
        .createdAt(Instant.parse("2021-11-08T07:30:00.00Z"));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}