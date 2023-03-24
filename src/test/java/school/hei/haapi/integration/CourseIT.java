package school.hei.haapi.integration;

import java.time.Instant;
import java.util.List;
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
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.GROUP1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
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


    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }


    @Test
    void read_course_ok() throws ApiException {
        ApiClient joeDoeClient = anApiClient(STUDENT1_TOKEN);
        TeachingApi api = new TeachingApi(joeDoeClient);

        List<Course> allCourses = api.getCourses(null, null, null, null, null, null, null, null, null);
        List<Course> coursesFilteredByCode =
                api.getCourses("prog", null, null, null, null, null, null, null, null);
        List<Course> coursesFilteredByName = api.getCourses(null, "Algorithmique", null, null, null,
                null,
                null,
                null, null);
        List<Course> coursesFilteredByCredits = api.getCourses(null, null, "6", null, null, null,
                null, null, null);
        List<Course> coursesFilteredByTeacherFirstName = api.getCourses(null, null, null, "Two", null,
                null, null, null, null);
        List<Course> coursesFilteredByTeacherLastName = api.getCourses(null, null, null, null,
                "teacher", null, null, null, null);
        List<Course> coursesOrderByCredits = api.getCourses(null, null, null, null, null, Order.ASC,
                null, null, null);
        Course firstCourseOrderByCredits = coursesOrderByCredits.get(0);
        Course secondCourseOrderByCredits = coursesOrderByCredits.get(1);
        List<Course> coursesOrderByCode = api.getCourses(null, null, null, null, null, null, Order.DESC,
                null, null);
        Course firstCourseOrderByCode = coursesOrderByCode.get(0);
        Course secondCourseOrderByCode = coursesOrderByCode.get(1);
        List<Course> coursesFilteredByTeacherFirstNameOrderByCreditsAndCode = api.getCourses(null,
                null, null, "One", null, Order.DESC, Order.ASC, 1, 20);
        Course previousCourse = null;
        for (Course course : coursesFilteredByTeacherFirstNameOrderByCreditsAndCode) {
            if (previousCourse != null) {
                assertTrue(course.getCredits() <= previousCourse.getCredits());
            }
            previousCourse = course;
        }
        previousCourse = null;
        for (Course course : coursesFilteredByTeacherFirstNameOrderByCreditsAndCode) {
            if (previousCourse != null && course.getCredits() == previousCourse.getCredits()) {
                assertTrue(course.getCode().compareTo(previousCourse.getCode()) >= 0);
            }

        assertEquals(3, allCourses.size());
        assertEquals(2, coursesFilteredByCode.size());
        assertEquals(1, coursesFilteredByName.size());
        assertEquals(2, coursesFilteredByCredits.size());
        assertEquals(1, coursesFilteredByTeacherFirstName.size());
        assertEquals(3, coursesFilteredByTeacherLastName.size());
        assertEquals(3, coursesOrderByCredits.size());
        assertTrue(firstCourseOrderByCredits.getCredits() < secondCourseOrderByCredits.getCredits());
        assertEquals(3, coursesOrderByCode.size());
        assertTrue(firstCourseOrderByCode.getCode().compareTo(secondCourseOrderByCode.getCode()) >= 0);
        assertEquals(2, coursesFilteredByTeacherFirstNameOrderByCreditsAndCode.size());
        assertTrue(course.getCode().compareTo(previousCourse.getCode()) >= 0);


        }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}

