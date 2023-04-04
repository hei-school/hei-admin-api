package school.hei.haapi.integration;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.Direction;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.CourseStatus.LINKED;
import static school.hei.haapi.endpoint.rest.model.CourseStatus.UNLINKED;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE3_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE4_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE5_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER2_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER3_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER4_ID;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
@Slf4j
public class CourseIT {

  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, CourseIT.ContextInitializer.SERVER_PORT);
  }

  public static Teacher teacher1() {
    Teacher teacher = new Teacher();
    teacher.setId(TEACHER1_ID);
    teacher.setFirstName("One");
    teacher.setLastName("Teacher");
    teacher.setEmail("test+teacher1@hei.school");
    teacher.setRef("TCR21001");
    teacher.setPhone("0322411125");
    teacher.setStatus(EnableStatus.ENABLED);
    teacher.setSex(Teacher.SexEnum.F);
    teacher.setBirthDate(LocalDate.parse("1990-01-01"));
    teacher.setEntranceDatetime(Instant.parse("2021-10-08T08:27:24.00Z"));
    teacher.setAddress("Adr 3");
    return teacher;
  }

  public static Teacher teacher2() {
    Teacher teacher = new Teacher();
    teacher.setId(TEACHER2_ID);
    teacher.setFirstName("Two");
    teacher.setLastName("Teacher");
    teacher.setEmail("test+teacher2@hei.school");
    teacher.setRef("TCR21002");
    teacher.setPhone("0322411126");
    teacher.setStatus(EnableStatus.ENABLED);
    teacher.setSex(Teacher.SexEnum.M);
    teacher.setBirthDate(LocalDate.parse("1990-01-02"));
    teacher.setEntranceDatetime(Instant.parse("2021-10-09T08:28:24Z"));
    teacher.setAddress("Adr 4");
    return teacher;
  }

  public static Teacher teacher3() {
    Teacher teacher = new Teacher();
    teacher.setId(TEACHER3_ID);
    teacher.setFirstName("Three");
    teacher.setLastName("Teach");
    teacher.setEmail("test+teacher3@hei.school");
    teacher.setRef("TCR21003");
    teacher.setPhone("0322411126");
    teacher.setStatus(EnableStatus.ENABLED);
    teacher.setSex(Teacher.SexEnum.M);
    teacher.setBirthDate(LocalDate.parse("1990-01-02"));
    teacher.setEntranceDatetime(Instant.parse("2021-10-09T08:28:24Z"));
    teacher.setAddress("Adr 4");
    return teacher;
  }

  public static Teacher teacher4() {
    Teacher teacher = new Teacher();
    teacher.setId(TEACHER4_ID);
    teacher.setFirstName("Four");
    teacher.setLastName("Binary");
    teacher.setEmail("test+teacher4@hei.school");
    teacher.setRef("TCR21004");
    teacher.setPhone("0322411426");
    teacher.setStatus(EnableStatus.ENABLED);
    teacher.setSex(Teacher.SexEnum.F);
    teacher.setBirthDate(LocalDate.parse("1990-01-04"));
    teacher.setEntranceDatetime(Instant.parse("2021-10-09T08:28:24Z"));
    teacher.setAddress("Adr 5");
    return teacher;
  }

  static Course course1() {
    Course course = new Course();
    course.setId(COURSE1_ID);
    course.setCode("PROG1");
    course.setCredits(6);
    course.setTotalHours(20);
    course.setMainTeacher(teacher2());
    course.setName("Algorithmics");
    return course;
  }

  static Course course2() {
    Course course = new Course();
    course.setId(COURSE2_ID);
    course.setCode("PROG3");
    course.setCredits(6);
    course.setTotalHours(20);
    course.setMainTeacher(teacher1());
    course.setName("Advanced OOP");
    return course;
  }

  static Course course3() {
    Course course = new Course();
    course.setId("course3_id");
    course.setCode("WEB1");
    course.setCredits(4);
    course.setTotalHours(16);
    course.setMainTeacher(teacher2());
    course.setName("Web Interface");
    return course;
  }


  static Course course4() {
    Course course = new Course();
    course.setId(COURSE4_ID);
    course.setCode("WEB2");
    course.setCredits(6);
    course.setTotalHours(18);
    course.setMainTeacher(teacher3());
    course.setName("Web Application");
    return course;
  }

  static Course course5() {
    Course course = new Course();
    course.setId(COURSE5_ID);
    course.setCode("MGT1");
    course.setCredits(5);
    course.setTotalHours(12);
    course.setMainTeacher(teacher4());
    course.setName("Collaborative work");
    return course;
  }

  static CrupdateCourse crupdatedCourse1() {
    CrupdateCourse crupdatedCourse = new CrupdateCourse();
    crupdatedCourse.setId(COURSE5_ID);
    crupdatedCourse.setCode("MGT1");
    crupdatedCourse.setName("Collaborative work");
    crupdatedCourse.setCredits(5);
    crupdatedCourse.setTotalHours(12);
    crupdatedCourse.setMainTeacherId(TEACHER4_ID);
    return crupdatedCourse;
  }

  static CrupdateCourse crupdatedCourse2() {
    CrupdateCourse crupdatedCourse = new CrupdateCourse();
    crupdatedCourse.setCode("MGT1");
    crupdatedCourse.setName("Collaborative work like GWSP");
    crupdatedCourse.setCredits(12);
    crupdatedCourse.setTotalHours(5);
    crupdatedCourse.setMainTeacherId(TEACHER4_ID);
    return crupdatedCourse;
  }

  static UpdateStudentCourse updateStudentCourse() {
    UpdateStudentCourse updated = new UpdateStudentCourse();
    updated.setCourseId(COURSE3_ID);
    updated.status(LINKED);
    return updated;
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  private boolean isBefore(String a, String b) {
    return a.compareTo(b) < 0;
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);

    List<Course> actual = api.getCourses(null, null, null,
        null, null, null, null, null, null);

    assertEquals(5, actual.size());
    assertTrue(actual.contains(course1()));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);

    List<Course> actual = api.getCourses(null, null, null,
        null, null, null, null, null, null);

    assertEquals(5, actual.size());
    assertTrue(actual.contains(course2()));
  }

  @Test
  void user_read_by_code() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<Course> actual = api.getCourses("PROG1", null, null,
        null, null, null, null, null, null);

    assertEquals(1, actual.size());
    assertTrue(actual.contains(course1()));
  }

  @Test
  void user_read_by_name() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<Course> actual = api.getCourses("PROG", null, null,
        null, null, null, null, null, null);

    assertEquals(2, actual.size());
    assertTrue(actual.contains(course1()));
    assertTrue(actual.contains(course2()));

  }

  @Test
  void user_read_by_teacher_last_name() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);

    List<Course> actual1 = api.getCourses(null, null, null,
        null, "ry", null, null, null, null);

    assertEquals(1, actual1.size());
    assertTrue(actual1.contains(course5()));
  }

  @Test
  void user_read_by_credits() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<Course> actual = api.getCourses(null, null, 6,
        null, null, null, null, null, null);

    assertEquals(3, actual.size());
    assertTrue(actual.contains(course1()));
    assertTrue(actual.contains(course2()));
    assertTrue(actual.contains(course4()));
  }

  @Test
  void user_read_ordered_by_code_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<Course> actual = api.getCourses(null, null, 6,
        null, null, null, Direction.ASC, null, null);

    assertEquals(3, actual.size());
    assertTrue(isBefore(actual.get(0).getCode(), actual.get(1).getCode()));
    assertTrue(isBefore(actual.get(1).getCode(), actual.get(2).getCode()));
  }

  @Test
  void user_read_ordered_by_credits_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<Course> actual = api.getCourses(null, null, 6,
        null, null, Direction.ASC, null, null, null);

    assertEquals(3, actual.size());
    assertTrue(isBefore(actual.get(0).getCode(), actual.get(1).getCode()));
    assertTrue(isBefore(actual.get(1).getCode(), actual.get(2).getCode()));
  }

  @Test
  void student_read_own_student_course_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    UsersApi api = new UsersApi(student1Client);

    List<Course> actual1 = api.getStudentCoursesById(STUDENT1_ID, LINKED);
    List<Course> actual2 = api.getStudentCoursesById(STUDENT1_ID, UNLINKED);

    assertEquals(1, actual1.size());
    assertTrue(actual1.contains(course1()));
    assertEquals(2, actual2.size());
    assertTrue(actual2.contains(course2()));
  }

  @Test
  void student_read_student_course_ko() throws ApiException {
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
    assertTrue(actual2.contains(course2()));

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
    assertTrue(actual2.contains(course3()));
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
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"code must be unique\"}",
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
