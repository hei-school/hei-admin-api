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
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.endpoint.rest.model.Course;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
class CourseIT {

    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    @MockBean
    private EventBridgeClient eventBridgeClientMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
    }
    public static Course course1() {
        Course course = new Course();
        course.setId("course1_id");
        course.setRef("PROG2");
        course.setName("Name of course one");
        course.setCredits(5000);
        course.setTotalHours(25);
        return course;
    }

    public static Course course2() {
        Course course = new Course();
        course.setId("course2_id");
        course.setRef("WEB1");
        course.setName("Name of course two");
        course.setCredits(2300);
        course.setTotalHours(24);
        return course;
    }
    public static Course someCreatableCourse() {
        Course course = new Course();
        course.setName("" + randomUUID());
        course.setRef("" + randomUUID());
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
        assertThrowsForbiddenException(api::getCourses);
    }

    @Test
    void badtoken_write_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);
        TeachingApi api = new TeachingApi(anonymousClient);
        assertThrowsForbiddenException(() -> api.createOrUpdateCourses(course1()));
    }

    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        TeachingApi api = new TeachingApi(student1Client);
        List<Course> actualCourse = api.getCourses();
        Course actual1 = api.getCourseById(COURSE1_ID);

        assertTrue(actualCourse.contains(course1()));
        assertTrue(actualCourse.contains(course2()));
        assertEquals(course1(), actual1);
    }

    @Test
    void student_write_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        TeachingApi api = new TeachingApi(student1Client);
        assertThrowsForbiddenException(() -> api.createOrUpdateCourses(course1()));
    }

    @Test
    void teacher_write_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        TeachingApi api = new TeachingApi(teacher1Client);

        assertThrowsForbiddenException(() -> api.createOrUpdateCourses(course1()));
    }

    @Test
    void manager_write_create_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);
        Course toCreate3 = someCreatableCourse();
        Course toCreate4 = someCreatableCourse();

        Course created1 = api.createOrUpdateCourses(toCreate3);
        Course created2 = api.createOrUpdateCourses(toCreate4);

        toCreate3.setId(created1.getId());
        toCreate3.setRef(created1.getRef());
        assertTrue(isValidUUID(created1.getId()));
        assertNotNull(created1.getRef());
        //
        assertEquals(created1, toCreate3);

        toCreate4.setId(created2.getId());
        toCreate4.setRef(created2.getRef());
        assertTrue(isValidUUID(created2.getId()));
        assertNotNull(created2.getRef());
        assertEquals(created2, toCreate4);
    }

    @Test
    void manager_write_update_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);
        Course toUpdate1 = api.createOrUpdateCourses(course1());
        Course toUpdate2 = api.createOrUpdateCourses(course2());
        toUpdate1.setName("A new name one");
        toUpdate2.setName("A new name two");

        Course updated1 = api.createOrUpdateCourses(course1());
        Course updated2 = api.createOrUpdateCourses(course2());
        List<Course> updated = new ArrayList<>();
        updated.add(updated1);
        updated.add(updated2);

        assertEquals(2, updated.size());
        assertTrue(updated.contains(toUpdate1));
        assertTrue(updated.contains(toUpdate2));
    }
    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
