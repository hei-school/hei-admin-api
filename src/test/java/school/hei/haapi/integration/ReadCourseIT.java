package school.hei.haapi.integration;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.exception.ApiException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = ReadCourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class ReadCourseIT {
    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    public static Course course1(){
        Course course = new Course();
        course.setId(COURSE1_ID);
        course.setCode("PROG1");
        course.setName("Algorithmics");
        course.setCredits(3);
        course.setTotalHours(40);
        course.setMainTeacher(TeacherIT.teacher1());
        return course;
    }

    public static Course course3(){
        Course course = new Course();
        course.setId(COURSE3_ID);
        course.setCode("MGT1");
        course.setName("name");
        course.setCredits(2);
        course.setTotalHours(20);
        course.setMainTeacher(TeacherIT.teacher2());
        return course;
    }

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, GroupIT.ContextInitializer.SERVER_PORT);
    }

    @Test
    void student_read_ok() throws school.hei.haapi.endpoint.rest.client.ApiException {
        ApiClient studen1Client = anApiClient(STUDENT1_TOKEN);

        TeachingApi api = new TeachingApi(studen1Client);
        Course actual1 = api.getCourseById(COURSE1_ID);
        List<Course> actualCourses = api.getCourses(null,null,"","","","","","","");

        assertEquals(course1(), actual1);
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course3()));
    }

    @Test
    void teacher_read_ok() throws school.hei.haapi.endpoint.rest.client.ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        TeachingApi api = new TeachingApi(teacher1Client);
        Course actual1 = api.getCourseById(COURSE1_ID);
        List<Course> actualCourses = api.getCourses(null,null,"","","","","","","");

        assertEquals(course1(), actual1);
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course3()));
    }

    @Test
    void manager_read_ok() throws school.hei.haapi.endpoint.rest.client.ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);

        TeachingApi api = new TeachingApi(manager1Client);
        Course actual1 = api.getCourseById(COURSE1_ID);
        List<Course> actualCourses = api.getCourses(null,null,"","","","","","","");

        assertEquals(course1(), actual1);
        assertTrue(actualCourses.contains(course1()));
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
