package school.hei.haapi.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.Student.SexEnum.F;
import static school.hei.haapi.integration.conf.TestUtils.FEE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.course1;
import static school.hei.haapi.integration.conf.TestUtils.course2;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
import school.hei.haapi.endpoint.rest.model.CourseSession;
import school.hei.haapi.endpoint.rest.model.CreateAttendanceMovement;
import school.hei.haapi.endpoint.rest.model.PlaceEnum;
import school.hei.haapi.endpoint.rest.model.Student;
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

    StudentAttendanceMovement actual = api.createAttendanceMovement(createAttendanceMovement());
    StudentAttendanceMovement expected = new StudentAttendanceMovement()
        .id("attendance1_id")
        .place(PlaceEnum.ANDRAHARO)
        .createdAt(Instant.parse("2021-11-08T07:30:00.00Z"))
        .student(student1())
        .attendanceMovementType(AttendanceMovementType.IN);

    assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    assertEquals(expected.getAttendanceMovementType(), actual.getAttendanceMovementType());
    assertEquals(expected.getPlace(), actual.getPlace());
    assertEquals(expected.getStudent(), actual.getStudent());
  }

  @Test
  void manager_create_attendance_with_no_student_id() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    AttendanceApi api = new AttendanceApi(manager1Client);

    assertThrowsApiException("{\"type\":\"400 BAD_REQUEST\",\"message\":\"the student with #student_id_ko doesn't exist\"}",
        () -> api.createAttendanceMovement(createAttendanceMovementKo()));
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

  public static Student student1() {
    return new Student()
        .id("student1_id")
        .address("Adr 1")
        .birthDate(LocalDate.parse("2000-01-01"))
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .sex(Student.SexEnum.M)
        .email("test+ryan@hei.school")
        .firstName("Ryan")
        .lastName("Andria")
        .ref("STD21001")
        .phone("0322411123")
        .status(ENABLED);
  }

  public static Student student2() {
    return new Student()
        .id("student2_id")
        .address("Adr 2")
        .birthDate(LocalDate.parse("2000-01-02"))
        .entranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"))
        .sex(F)
        .email("test+student2@hei.school")
        .firstName("Two")
        .lastName("Student")
        .ref("STD21002")
        .phone("0322411124")
        .status(ENABLED);
  }

  public static Student student3() {
    return new Student()
        .id("student3_id")
        .address("Adr 2")
        .birthDate(LocalDate.parse("2000-01-02"))
        .entranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"))
        .sex(F)
        .email("test+student3@hei.school")
        .firstName("Three")
        .lastName("Student")
        .ref("STD21003")
        .phone("0322411124")
        .status(ENABLED);
  }

  public static StudentAttendance attendance1Ok() {
    return new StudentAttendance()
        .id("attendance1_id")
        .student(student1())
        .place(PlaceEnum.ANDRAHARO)
        .isLate(false)
        .courseSession(courseSession1())
        .attendanceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
  }

  public static StudentAttendance attendance2Ok() {
    return new StudentAttendance()
        .id("attendance2_id")
        .student(student1())
        .place(PlaceEnum.ANDRAHARO)
        .isLate(false)
        .courseSession(courseSession2())
        .attendanceDatetime(Instant.parse("2021-08-08T08:25:24.00Z"));
  }

  public static StudentAttendance attendance3Late() {
    return new StudentAttendance()
        .id("attendance3_id")
        .place(PlaceEnum.IVANDRY)
        .isLate(true)
        .courseSession(courseSession1())
        .student(student2())
        .attendanceDatetime(Instant.parse("2021-11-08T09:25:24.00Z"));
  }

  public static StudentAttendance attendance4Late() {
    return new StudentAttendance()
        .id("attendance4_id")
        .isLate(true)
        .place(PlaceEnum.ANDRAHARO)
        .courseSession(courseSession2())
        .student(student2())
        .attendanceDatetime(Instant.parse("2021-11-08T10:30:24.00Z"));
  }

  public static StudentAttendance attendance5Missing() {
    return new StudentAttendance()
        .id("attendance5_id")
        .isLate(false)
        .student(student3())
        .courseSession(courseSession1())
        .attendanceDatetime(null);
  }

  public static StudentAttendance attendance6Missing() {
    return new StudentAttendance()
        .id("pointing6_id")
        .isLate(false)
        .student(student3())
        .courseSession(courseSession2())
        .attendanceDatetime(null);
  }

  public static StudentAttendance attendance7Out() {
    return new StudentAttendance()
        .id("attendance7_id")
        .attendanceDatetime(Instant.parse("2021-11-08T012:30:00.00Z"))
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
