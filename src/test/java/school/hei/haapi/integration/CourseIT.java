package school.hei.haapi.integration;

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
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.GROUP1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
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
        course.setCode("PROG1");
        course.setName("Algorithmics");
        course.setCredits(6);
        course.setTotalHours(20);
        course.setMainTeacher(TeacherIT.teacher1());
        return course;
    }

    public static Course course2() {
        Course course = new Course();
        course.setId("course2_id");
        course.setCode("WEB1");
        course.setName("Web interface");
        course.setCredits(6);
        course.setTotalHours(24);
        course.setMainTeacher(TeacherIT.teacher2());
        return course;
    }

    public static Course course3() {
            Course course = new Course();
            course.setId("course3_id");
            course.setCode("SYS1");
            course.setName("Operating system");
            course.setCredits(6);
            course.setTotalHours(22);
            course.setMainTeacher(TeacherIT.teacher3());
            return course;
        }

    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void badtoken_read_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);

        TeachingApi api = new TeachingApi(anonymousClient);
        assertThrowsForbiddenException(() -> api.getCourses(1,15));
    }

    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        TeachingApi api = new TeachingApi(student1Client);
        List<Course> actualCourses = api.getCourses(1,15);
        Course actualCourse1 = actualCourses.get(0);

        assertEquals(course1(), actualCourse1);
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course2()));
        assertTrue(actualCourses.contains(course3()));
    }

    @Test
    void teacher_read_ok() throws ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        TeachingApi api = new TeachingApi(teacher1Client);
        List<Course> actualCourses = api.getCourses(1,15);
        Course actualCourse1 = actualCourses.get(0);

        assertEquals(course1(), actualCourse1);
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course2()));
        assertTrue(actualCourses.contains(course3()));
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
