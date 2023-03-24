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
import school.hei.haapi.endpoint.rest.model.CodeOrder;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CreditsOrder;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
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
        course.setCredits(7);
        course.setTotalHours(24);
        course.setMainTeacher(TeacherIT.teacher2());
        return course;
    }

    public static Course course3() {
        Course course = new Course();
        course.setId("course3_id");
        course.setCode("SYS1");
        course.setName("Operating system");
        course.setCredits(8);
        course.setTotalHours(22);
        course.setMainTeacher(TeacherIT.teacher3());
        return course;
    }
    public static Course course4() {
        Course course = new Course();
        course.setId("course4_id");
        course.setCode("PROG2");
        course.setName("Object-oriented programming");
        course.setCredits(6);
        course.setTotalHours(21);
        course.setMainTeacher(TeacherIT.teacher1());
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
        assertThrowsForbiddenException(() -> api.getCourses(
                1,
                15,
                null,
                null,
                null,
                null,
                null,
                null,
                null));
    }

    @Test
    void read_without_params_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);

        TeachingApi api = new TeachingApi(manager1Client);
        List<Course> actualCourses = api.getCourses(
                1,
                20,
                null,
                null,
                null,
                null,
                null,
                null ,
                null);
        Course actualCourse1 = actualCourses.get(0);

        assertEquals(course1(), actualCourse1);
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course2()));
        assertTrue(actualCourses.contains(course3()));
        assertTrue(actualCourses.contains(course4()));
    }

    @Test
    void read_with_code_ok() throws ApiException{
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);

        List<Course> actualCourses = api.getCourses(
                1,
                20,
                "PrOg",
                null,
                null,
                null,
                null,
                null ,
                null);

        assertEquals(2, actualCourses.size());
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course4()));
    }

    @Test
    void read_with_name() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);

        List<Course> actualCourses = api.getCourses(
                1,
                20,
                null,
                "alGO",
                null,
                null,
                null,
                null ,
                null);

        assertEquals(1, actualCourses.size());
        assertTrue(actualCourses.contains(course1()));
    }

    @Test
    void read_with_credits() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);

        List<Course> actualCourses = api.getCourses(
                1,
                20,
                null,
                null,
                6,
                null,
                null,
                null ,
                null);

        assertEquals(2, actualCourses.size());
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course4()));
    }

    @Test
    void read_with_teacher_first_name() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);

        List<Course> actualCourses = api.getCourses(
                1,
                20,
                null,
                null,
                null,
                "OnE",
                null,
                null ,
                null);

        assertEquals(2, actualCourses.size());
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course4()));
    }

    @Test
    void read_with_teacher_last_name() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);

        List<Course> actualCourses = api.getCourses(
                1,
                20,
                null,
                null,
                null,
                null,
                "tEacHEr",
                null ,
                null);

        assertEquals(3, actualCourses.size());
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course2()));
        assertTrue(actualCourses.contains(course4()));
    }

    @Test
    void read_with_credits_order_asc() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);

        List<Course> actualCourses = api.getCourses(
                1,
                20,
                null,
                null,
                null,
                null,
                null,
                CreditsOrder.ASC,
                null);

        assertEquals(4, actualCourses.size());
        assertEquals(course1(), actualCourses.get(0));
        assertEquals(course3(), actualCourses.get(3));
    }

    @Test
    void read_with_credits_order_desc() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);

        List<Course> actualCourses = api.getCourses(
                1,
                20,
                null,
                null,
                null,
                null,
                null,
                CreditsOrder.DESC,
                null);

        assertEquals(4, actualCourses.size());
        assertEquals(course3(), actualCourses.get(0));
        assertEquals(course4(), actualCourses.get(3));
    }

    @Test
    void read_with_code_order_asc() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);

        List<Course> actualCourses = api.getCourses(
                1,
                20,
                null,
                null,
                null,
                null,
                null,
                null,
                CodeOrder.ASC);

        assertEquals(4, actualCourses.size());
        assertEquals(course1(), actualCourses.get(0));
        assertEquals(course2(), actualCourses.get(3));
    }

    @Test
    void read_with_code_order_desc() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);

        List<Course> actualCourses = api.getCourses(
                1,
                20,
                null,
                null,
                null,
                null,
                null,
                null,
                CodeOrder.DESC);

        assertEquals(4, actualCourses.size());
        assertEquals(course2(), actualCourses.get(0));
        assertEquals(course1(), actualCourses.get(3));
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}