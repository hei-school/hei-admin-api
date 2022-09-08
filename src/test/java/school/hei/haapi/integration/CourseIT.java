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
import school.hei.haapi.endpoint.rest.model.Group;
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
@ContextConfiguration(initializers = SecurityIT.ContextInitializer.class)
@AutoConfigureMockMvc
class CourseIT {

    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    @MockBean
    private EventBridgeClient eventBridgeClientMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, school.hei.haapi.integration.StudentIT.ContextInitializer.SERVER_PORT);
    }
    public static Course course1() {
        Course course = new Course();
        course.setId("course1_id");
        course.setRef("Name of course one");
        course.setName("PROG1");
        course.setCredits(5000);
        course.setTotalHours(24);
        return course;
    }

    public static Course course2() {
        Course course = new Course();
        course.setId("course2_id");
        course.setRef("Name of course two");
        course.setName("PROG2");
        course.setCredits(2300);
        course.setTotalHours(24);
        return course;
    }
    public static Course someCreatableCourse() {
        Course course = new Course();
        course.setName("Some name");
        course.setRef("CRS21-" + randomUUID());
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
        Course actual1 = api.getCourseById(COURSE1_ID);
        List<Course> actualCourse = api.getCourses();

        assertEquals(course1(), actual1);
        assertTrue(actualCourse.contains(course1()));
        assertTrue(actualCourse.contains(course2()));
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
        Course toCreate3 = someCreatableCourse();
        Course toCreate4 = someCreatableCourse();

        TeachingApi api = new TeachingApi(manager1Client);
        Course created = api.createOrUpdateCourses(toCreate3);

        Course created3 = toCreate3;
        toCreate3.setId(created3.getId());
        toCreate3.setName(created3.getName());
        assertTrue(isValidUUID(created3.getId()));
        assertNotNull(created3.getName());
        //
        assertEquals(created3, toCreate3);

        Course created4 = toCreate4;
        toCreate4.setId(created4.getId());
        toCreate4.setName(created4.getName());
        assertTrue(isValidUUID(created4.getId()));
        assertNotNull(created4.getName());
        assertEquals(created4, toCreate4);
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
