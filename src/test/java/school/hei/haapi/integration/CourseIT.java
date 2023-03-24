package school.hei.haapi.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.Order;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.TeacherIT.teacher1;
import static school.hei.haapi.integration.TeacherIT.teacher2;
import static school.hei.haapi.integration.conf.TestUtils.*;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = FeeIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class CourseIT {
    @MockBean
    private SentryConf sentryConf;
    @MockBean
    private CognitoComponent cognitoComponentMock;

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }

        private static ApiClient anApiClient(String token) {
            return TestUtils.anApiClient(token, CourseIT.ContextInitializer.SERVER_PORT);
        }

        static Course course1() {
            Course course = new Course();
            course.setId("1");
            course.setCode("PROG1");
            course.setCredits(13);
            course.setName("Algorithms");
            course.setTotalHours(21);
            course.setMainTeacher(teacher1());
            return course;
        }
        static Course course2() {
            Course course = new Course();
            course.setId("2");
            course.setCode("PROG3");
            course.setCredits(6);
            course.setName("POO");
            course.setTotalHours(20);
            course.setMainTeacher(teacher2());
            return course;
        }
        static Course course3() {
            Course course = new Course();
            course.setId("3");
            course.setCode("WEB1");
            course.setCredits(10);
            course.setName("Interface");
            course.setTotalHours(15);
            course.setMainTeacher(teacher1());
            return course;
        }

        @Test
        void course_read_ok() throws ApiException {
            ApiClient teach1Client = anApiClient(TEACHER1_TOKEN);
            TeachingApi api = new TeachingApi(teach1Client);

            List<Course> actualCourse = api.getCourses(1, 6, "PROG1", "Algorithms", 13, "One", "Teacher", Order.DESC, Order.DESC);

            assertTrue(actualCourse.contains(course1()));
        }

    }
}
