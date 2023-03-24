package school.hei.haapi.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Courses;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static school.hei.haapi.integration.conf.TestUtils.*;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class CourseIT {
    public static final String MANAGER1_TOKEN = "manager1_token";
    public static final String COURSES_TOKEN = "course1_token";
    public static final String COURSES_FILTER = "Courses_filter";

    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    @MockBean
    private EventBridgeClient eventBridgeClientMock;
    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, StudentIT.ContextInitializer.SERVER_PORT);
    }
    public static Courses Course1(){
        Courses courses = new Courses();
        courses.setId("1");
        courses.setCode("PROG1");
        courses.setCredits(6);
        courses.setName("Algorithme");
        courses.setMain_teacher(User.builder()
                        .id("1")
                .build());
        return courses;
    }
    public static Courses Course2(){
        Courses courses = new Courses();
        courses.setId("2");
        courses.setCode("PROG3");
        courses.setCredits(6);
        courses.setName("P.O.O avancee");
        courses.setMain_teacher(User.builder()
                .id("2")
                .build());
        return courses;
    }
    public static Courses Course3(){
        Courses courses = new Courses();
        courses.setId("3");
        courses.setCode("WEB1");
        courses.setCredits(4);
        courses.setName("Interface Web");
        courses.setMain_teacher(User.builder()
                .id("1")
                .build());
        return courses;
    }

    @Test
    void courses_read_ok() throws ApiException{
        ApiClient courses1 = anApiClient(COURSES_TOKEN);
        CoursesApi api =  new CoursesApi(courses1);
        Courses actual = api.getCoursesByFilter(COURSES_FILTER);

        assertEquals(course1(), actual);
    }
    @Test
    void courses_read_ok() {
        ApiClient courses1Client = anApiClient(COURSES_TOKEN);

        CoursesApi api = new CoursesApi(courses1Client);
        assertThrowsForbiddenException(
                () -> api.getByFilter(COURSES_FILTER));
        assertThrowsForbiddenException(
                () -> api.getCourses(1, "DONNE-3", null, null, null));
    }
    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}

