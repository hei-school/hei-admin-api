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
import static org.junit.jupiter.api.Assertions.assertEquals;

import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.util.List;

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

    public static Course course1(){
        Course course = new Course();
        course.setId("courses1_id");
        course.setCode("PROG1");
        course.setName("Algorithmics");
        course.setCredits(3);
        course.setTotalHours(40);
        course.setMainTeacher(TeacherIT.teacher1());
        return course;
    }

    public static Course course3(){
        Course course = new Course();
        course.setId("courses3_id");
        course.setCode("MGT1");
        course.setName("name");
        course.setCredits(2);
        course.setTotalHours(20);
        course.setMainTeacher(TeacherIT.teacher2());
        return course;
    }

    public static CrupdateCourse someCreatableCourse() {
        CrupdateCourse newCourse = new CrupdateCourse();
        newCourse.setName("some course");
        newCourse.setCredits(10);
        newCourse.setTotalHours(200);
        newCourse.setCode("code");
        newCourse.setMainTeacherId(TEACHER2_ID);
        return newCourse;
    }

    public static Course someCreatedCourse(){
        Course course = new Course();
        course.setMainTeacher(TeacherIT.teacher2());
        course.setCode("code");
        course.setCredits(10);
        course.setTotalHours(200);
        course.setName("some course");
        return course;
    }

    @BeforeEach
    void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void student_read_ok() throws school.hei.haapi.endpoint.rest.client.ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        TeachingApi api = new TeachingApi(student1Client);
        Course actual1 = api.getCourseById(COURSE1_ID);
        List<Course> actualCourses = api.getCourses(null, null, null, null, null, null, null,null,null);

        assertEquals(course1(), actual1);
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course3()));
    }

    @Test
    void teacher_read_ok() throws school.hei.haapi.endpoint.rest.client.ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        TeachingApi api = new TeachingApi(teacher1Client);
        Course actual1 = api.getCourseById(COURSE1_ID);
        List<Course> actualCourses = api.getCourses(null,null, null, null, null, null,null,null, null);

        assertEquals(course1(), actual1);
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course3()));
    }

    @Test
    void manager_read_ok() throws school.hei.haapi.endpoint.rest.client.ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);

        TeachingApi api = new TeachingApi(manager1Client);
        Course actual1 = api.getCourseById(COURSE1_ID);
        List<Course> actualCourses = api.getCourses(null, null, null, null, null, null, null,null,null);

        assertEquals(course1(), actual1);
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course3()));
    }

    @Test
    void student_write_update_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TeachingApi api = new TeachingApi(student1Client);

        assertThrowsForbiddenException(() -> api.createOrUpdateGroups(List.of()));
    }

    @Test
    void teacher_write_update_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        TeachingApi api = new TeachingApi(teacher1Client);

        assertThrowsForbiddenException(() -> api.createOrUpdateGroups(List.of()));
    }

    @Test
    void manager_write_update_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);
        List<Course> toCrupdate = api.crupdateCourses(List.of(someCreatableCourse()));
        Course expected = someCreatedCourse();

        assertEquals(1, toCrupdate.size());
        assertTrue(toCrupdate.contains(expected));
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
