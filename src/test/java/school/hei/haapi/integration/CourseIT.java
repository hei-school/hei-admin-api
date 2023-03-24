package school.hei.haapi.integration;

import java.util.List;
import java.util.Objects;

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
import school.hei.haapi.endpoint.rest.client.ApiResponse;
import school.hei.haapi.endpoint.rest.model.*;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.TeacherIT.teacher1;
import static school.hei.haapi.integration.TeacherIT.teacher2;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

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
  private final String STUDENT1_ID = "student1_id";

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, CourseIT.ContextInitializer.SERVER_PORT);
  }

  private Course course1() {
    return new Course()
            .id("course1_id")
            .code("PROG1")
            .name("Algorithme et structure de données")
            .mainTeacher(teacher1())
            .credits(10)
            .totalHours(200);
  }

  private Course course2() {
    return new Course()
            .id("course2_id")
            .code("WEB1")
            .name("Interface homme machine")
            .mainTeacher(teacher2())
            .credits(8)
            .totalHours(150);
  }

  private CrupdateCourse new_course() {
    return new CrupdateCourse()
            .id("course3_id")
            .code("SYS1")
            .name("Systeme d'exploitation")
            .credits(10)
            .totalHours(200)
            .mainTeacherId("teacher1_id");
  }

  private CrupdateCourse to_be_updated() {
    return new CrupdateCourse()
            .id("course1_id")
            .code("PROG2")
            .name("Base de données")
            .credits(10)
            .totalHours(200)
            .mainTeacherId("teacher1_id");
  }

  private Course updated() {
    return new Course()
            .id("course1_id")
            .code("PROG2")
            .name("Base de données")
            .credits(10)
            .totalHours(200)
            .mainTeacher(teacher1());
  }


  private UpdateStudentCourse courseToUpdate() {
    return new UpdateStudentCourse()
            .courseId("course2_id")
            .status(CourseStatus.UNLINKED);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void get_student_followed_courses_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    UsersApi api = new UsersApi(student1Client);
    List<Course> coursesLinked = api.getStudentCoursesById(STUDENT1_ID, CourseStatus.LINKED);
    List<Course> coursesUnlinked = api.getStudentCoursesById(STUDENT1_ID, CourseStatus.UNLINKED);

    assertEquals(coursesUnlinked, List.of(course2()));
    assertEquals(coursesLinked, List.of(course1()));
  }

  @Test
  void get_all_courses_ok() throws ApiException{
    ApiClient teacherClient = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacherClient);
    List<Course> courses = api.getCourses(1, 15, null, null,null,null,null, null, null);

    log.info("courses{}" , courses);

    assertTrue(courses.contains(course1()));
    assertTrue(courses.contains(course2()));
  }

  @Test
  void read_courses_by_name_ok() throws ApiException{
    ApiClient teacherClient = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacherClient);
    List<Course> courses = api.getCourses(1,15, null, course2().getName(), course2().getCredits(), null , null, null, null);

    log.info("courses{}" , courses);

    assertFalse(courses.contains(course1()));
    assertTrue(courses.contains(course2()));
  }
  @Test
  void read_courses_by_credits_ok() throws ApiException{
    ApiClient teacherClient = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacherClient);
    List<Course> courses = api.getCourses(1,15, null, null, course2().getCredits(), null , null, null, null);

    log.info("courses{}" , courses);

    assertTrue(courses.contains(course2()));
    assertFalse(courses.contains(course1()));
  }
  @Test
  void read_courses_by_code_ok() throws ApiException{
    ApiClient teacherClient = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacherClient);
    List<Course> courses = api.getCourses(1,15, course2().getCode(), null, null, null , null, null, null);

    log.info("courses{}" , courses);

    assertEquals(courses.get(0), course2());
  }
  @Test
  void read_courses_by_teacher_name_ok() throws ApiException{
    ApiClient teacherClient = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacherClient);
    List<Course> courses = api.getCourses(1,15, null, null, null, Objects.requireNonNull(course2().getMainTeacher()).getFirstName() , Objects.requireNonNull(course2().getMainTeacher()).getLastName(), null, null);

    log.info("courses{}" , courses);

    assertEquals(courses.get(0), course2());
  }


  @Test
  void get_all_courses_order_by_credits_desc() throws ApiException{
    ApiClient teacherClient = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacherClient);
    List<Course> courses = api.getCourses(1,15, null, null, null, null, null, OrderType.DESC, null);

    assertEquals(courses.get(0),course2() );
  }

  @Test
  void get_all_courses_order_by_credits_asc() throws ApiException{
    ApiClient teacherClient = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacherClient);
    List<Course> courses = api.getCourses(1,15, null, null, null, null, null, OrderType.ASC, null);

    assertEquals(courses.get(0),course2() );
  }

  @Test
  void get_all_courses_order_by_code_desc() throws ApiException{
    ApiClient teacherClient = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacherClient);
    List<Course> courses = api.getCourses(1,15, null, null, null, null, null, null, OrderType.DESC);

    assertEquals(courses.get(0),course1() );
  }

  @Test
  void get_all_courses_order_by_code_asc() throws ApiException{
    ApiClient teacherClient = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacherClient);
    List<Course> courses = api.getCourses(1,15, null, null, null, null, null, null, OrderType.ASC);

    assertEquals(courses.get(0),course1() );
  }

  @Test
  void put_student_followed_courses_ok() throws ApiException{
    ApiClient student1  = anApiClient(TEACHER1_TOKEN);
    UsersApi api = new UsersApi(student1);
    TeachingApi api1 = new TeachingApi(student1);

    List<Course> getCoursesBefore = api1.getCourses(1, 15, "", "", null, "", "", null, null);
    List<Course> courseToUpdate = api.updateStudentCourses(STUDENT1_ID,
        List.of(courseToUpdate()));
    List<Course> getAllCourses = api1.getCourses(1, 15, "", "", null, "", "", null, null);

    log.info("before: {}", getCoursesBefore);
    log.info("updated: {}", courseToUpdate);
    log.info("after: {}", getAllCourses);

  }

  @Test
  void crupdate_courses_ok () throws ApiException{
    ApiClient teacher1 = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1);
    List<Course> toBeCreated = api.crupdateCourses(List.of(new_course()));
    List<Course> toBeUpdated = api.crupdateCourses(List.of(to_be_updated()));

    List<Course> allCourses = api.getCourses(1, 15, null, null, null, null, null, null, null);
    log.info("courses{}", allCourses);

    assertTrue(allCourses.contains(toBeCreated.get(0)));
    assertTrue(allCourses.contains(toBeUpdated.get(0)));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
