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
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

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
        course.setRef("PROG1");
        course.setName("Algorithmics");
        course.setCredits(5);
        course.setTotalHours(45);
        return course;
    }

    public static Course course2() {
        Course course = new Course();
        course.setId("course2_id");
        course.setRef("SYS1");
        course.setName("Operating systems");
        course.setCredits(4);
        course.setTotalHours(40);
        return course;
    }

    public static Course someCreatableCourse(){
        Course course = new Course();
        course.setRef("COURSE21-" + randomUUID());
        course.setName("Some name");
        course.setCredits(6);
        course.setTotalHours(40);
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
        List<Course> actualCourses = api.getCourses();

        assertEquals(course1(), actual1);
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course2()));
    }

    @Test
    void student_write_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        TeachingApi api = new TeachingApi(student1Client);
        assertThrowsForbiddenException(() -> api.createOrUpdateCourses(new Course()));
    }

    @Test
    void teacher_read_ok() throws ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        TeachingApi api = new TeachingApi(teacher1Client);
        Course actual2 = api.getCourseById(COURSE2_ID);

        List<Course> actualCourses = api.getCourses();

        assertEquals(course2(), actual2);
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course2()));
    }

    @Test
    void teacher_write_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        TeachingApi api = new TeachingApi(teacher1Client);
        assertThrowsForbiddenException(() -> api.createOrUpdateCourses(new Course()));
    }

    @Test
    void manager_write_create_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);
        Course toCreate3 = someCreatableCourse();
        Course toCreate4 = someCreatableCourse();
        List<Course> created = new ArrayList<>();

        created.add(api.createOrUpdateCourses(toCreate3));
        created.add(api.createOrUpdateCourses(toCreate4));

        assertEquals(2, created.size());
        Course created3 = created.get(0);
        assertTrue(isValidUUID(created3.getId()));
        toCreate3.setId(created3.getId());
        assertEquals(created3, toCreate3);
        Course created4 = created.get(0);
        assertEquals(created4, toCreate3);
    }

    @Test
    void manager_write_update_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);
        List<Course> toUpdate = new ArrayList<>();
        toUpdate.add(api.createOrUpdateCourses(someCreatableCourse()));
        toUpdate.add(api.createOrUpdateCourses(someCreatableCourse()));
        Course toUpdate0 = toUpdate.get(0);
        toUpdate0.setName("A new name zero");
        Course toUpdate1 = toUpdate.get(1);
        toUpdate1.setName("A new name one");
        List<Course> updated = new ArrayList<>();

        updated.add(api.createOrUpdateCourses(toUpdate.get(0)));
        updated.add(api.createOrUpdateCourses(toUpdate.get(1)));

        assertEquals(2, updated.size());
        assertTrue(updated.contains(toUpdate0));
        assertTrue(updated.contains(toUpdate1));
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
