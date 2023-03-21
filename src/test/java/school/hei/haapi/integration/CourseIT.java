package school.hei.haapi.integration;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.CourseFollowed;
import school.hei.haapi.model.CourseFollowedRest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.*;

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

  private Course student1_linked(){
    return new Course()
        .id("course1_id")
        .code("PROG1")
        .name("Algorithme et structure de données")
        .mainTeacher(new Teacher()
            .id("teacher1_id")
            .ref("TCR21001")
            .firstName("One")
            .lastName("Teacher")
            .sex(Teacher.SexEnum.F)
            .birthDate(LocalDate.parse("1990-01-01"))
            .email("test+teacher1@hei.school")
            .address("Adr 3")
            .phone("0322411125")
            .entranceDatetime(Instant.parse("2021-10-08T08:27:24Z"))
            .status(EnableStatus.ENABLED))
        .credits(10)
        .totalHours(200);
  }

  private Course student1_unlinked(){
    return new Course()
        .id("course2_id")
        .code("WEB1")
        .name("Interface homme machine")
        .mainTeacher(new Teacher()
            .id("teacher2_id")
            .ref("TCR21002")
            .firstName("Two")
            .lastName("Teacher")
            .sex(Teacher.SexEnum.M)
            .birthDate(LocalDate.parse("1990-01-02"))
            .email("test+teacher2@hei.school")
            .address("Adr 4")
            .phone("0322411126")
            .entranceDatetime(Instant.parse("2021-10-09T08:28:24Z"))
            .status(EnableStatus.ENABLED))
        .credits(8)
        .totalHours(150);
  }

  private CrupdateCourse new_course(){
    return new CrupdateCourse()
        .id("course3_id")
        .code("SYS1")
        .name("Systeme d'exploitation")
        .credits(10)
        .totalHours(200)
        .mainTeacherId("teacher1_id");
  }

  private CrupdateCourse to_be_updated(){
    return new CrupdateCourse()
        .id("course1_id")
        .code("PROG2")
        .name("Base de données")
        .credits(10)
        .totalHours(200)
        .mainTeacherId("teacher1_id");
  }

  private UpdateStudentCourse courseToUpdate(){
    return new  UpdateStudentCourse()
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

    assertEquals(coursesUnlinked, List.of(student1_unlinked()));
    assertEquals(coursesLinked, List.of(student1_linked()));
  }

  @Test
  void get_all_courses_ok() throws ApiException{
    ApiClient student1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);
    List<Course> courses = api.getCourses(1, 15);

    assertTrue(courses.contains(student1_linked()));
    assertTrue(courses.contains(student1_unlinked()));
  }

  @Test
  void put_student_followed_courses_ok() throws ApiException{
    ApiClient student1  = anApiClient(TEACHER1_TOKEN);
    UsersApi api = new UsersApi(student1);
    TeachingApi api1 = new TeachingApi(student1);

    List<Course> getCoursesBefore = api1.getCourses(1, 15);
    List<Course> courseToUpdate = api.updateStudentCourses(STUDENT1_ID,
        List.of(courseToUpdate()));
    List<Course> getAllCourses = api1.getCourses(1, 15);

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
    List<Course> allCourses = api.getCourses(1, 15);
    log.info("courses{}", allCourses);

    assertEquals(allCourses.get(0), student1_unlinked());
    assertEquals(allCourses.get(1), toBeCreated.get(0));
    assertEquals(allCourses.get(2), toBeUpdated.get(0));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
