package school.hei.haapi.integration;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
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
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.CourseStatus.LINKED;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.teacher1;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
class CourseIT {
    @MockBean
    private SentryConf sentryConf;
    @MockBean
    private CognitoComponent cognitoComponentMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, CourseIT.ContextInitializer.SERVER_PORT);
    }

    static Course course() {
        return new Course()
                .id("course1_id")
                .code("PROG1")
                .name("Algorithmics")
                .credits(5)
                .totalHours(40)
                .mainTeacher(teacher1());
    }
    public static Course course1() {
        Course course = new Course();
        course.setId("course1_id");
        course.setName("Name of course one");
        course.setCode("CRS21001");
        course.setCredits(1);
        course.setTotalHours(1);
        course.setMainTeacher(teacher1());
        return course;
    }

    static UpdateStudentCourse courseToUpdate() {
        return new UpdateStudentCourse()
                .courseId("course1_id")
                .status(LINKED);
    }

    @BeforeEach
    void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void course_read_ok() throws ApiException {
        ApiClient teachingClient = anApiClient(TEACHER1_TOKEN);
        TeachingApi api = new TeachingApi(teachingClient);

        List<Course> actual = api.getCourses(1, 20, null,null,null,null,null,null);

        assertTrue(actual.contains(course()));
    }

    @Test
    void order_course_read_ok() throws ApiException {
        ApiClient teachingClient = anApiClient(TEACHER1_TOKEN);
        TeachingApi api = new TeachingApi(teachingClient);
    }
    @Test
    void order_course_read_ko() throws ApiException {
        ApiClient teachingClient = anApiClient(TEACHER1_TOKEN);
        TeachingApi api = new TeachingApi(teachingClient);

        assertThrowsApiException( "{\"type\":\"404 NOT_FOUND\",\"message\":\"user not found\"}" ,  () -> api.getCourses( course1().getCode(),
                course1().getName(),
                course1().getCredits(),
                course1().getMainTeacher().getFirstName(), course1().getMainTeacher().getLastName(),
                creditsOrderDESC,
                codeOrderASC,
                1,
                15));
    }


    @Test
    void manager_update_student_course_ko() throws ApiException {
        ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
        UsersApi api = new UsersApi(managerClient);
        String randomId = String.valueOf(randomUUID());

        Executable executable1 = () -> api.updateStudentCourses(STUDENT1_ID,
                List.of(new UpdateStudentCourse().courseId("random_course_id").status(LINKED)));
        Executable executable2 = () -> api.updateStudentCourses(randomId,
                List.of(courseToUpdate()));

        assertThrowsApiException(
                "{\"type\":\"404 NOT_FOUND\",\"message\":\"Course.random_course_id is not found.\"}",
                executable1);
        assertThrowsApiException(
                "{\"type\":\"404 NOT_FOUND\",\"message\":\"Student." + randomId + " is not found.\"}",
                executable2);
    }

    @Test
    void teacher_update_student_course_ko() throws ApiException {
        ApiClient teacherClient = anApiClient(TEACHER1_TOKEN);
        UsersApi api = new UsersApi(teacherClient);

        assertThrowsApiException(
                "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                () -> api.updateStudentCourses(STUDENT1_ID, List.of(courseToUpdate())));
    }

    @Test
    void student_update_student_course_ko() throws ApiException {
        ApiClient studentClient = anApiClient(STUDENT1_TOKEN);
        UsersApi api = new UsersApi(studentClient);

        assertThrowsApiException(
                "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                () -> api.updateStudentCourses(STUDENT1_ID, List.of(courseToUpdate())));
    }

    @Test
    void update_course_ok() throws ApiException {
        ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(managerClient);

        List<Course> actual = api.crupdateCourses(List.of(new CrupdateCourse()
                .id("course1_id")
                .code("SYS1")
                .name("Operating System")
                .credits(5)
                .totalHours(40)
                .mainTeacherId("teacher1_id")));

        assertFalse(actual.isEmpty());
        assertTrue(actual.contains(course()
                .code("SYS1")
                .name("Operating System")));
    }

    @Test
    void student_course_read_ko() throws ApiException {
        String new_student_id = "100001";
        ApiClient studentClient = anApiClient(STUDENT1_TOKEN);
        UsersApi api = new UsersApi(studentClient);

        assertThrowsApiException(
                "{\"type\":\"404 NOT_FOUND\",\"message\":\"Student.100001 is not found.\"}",
                () -> api.getStudentCoursesById(new_student_id, LINKED));
    }

    @Test
    void manager_update_student_course_ok() throws ApiException {
        ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
        UsersApi api = new UsersApi(managerClient);

        List<Course> actual = api.updateStudentCourses(STUDENT1_ID, List.of(courseToUpdate()));

        assertFalse(actual.isEmpty());
        assertTrue(actual.contains(course()));
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
