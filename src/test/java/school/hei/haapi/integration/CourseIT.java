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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
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
        course.setName("Algorithmique");
        course.setCode("PROG1");
        course.setCredits(6);
        course.setTotalHours(12);
        course.setMainTeacher(TeacherIT.teacher1());
        return course;
    }

    public static Course course2() {
        Course course = new Course();
        course.setId("course2_id");
        course.setName("P.O.O avancée");
        course.setCode("PROG3");
        course.setCredits(6);
        course.setTotalHours(48);
        course.setMainTeacher(TeacherIT.teacher2());
        return course;
    }

    public static Course course3() {
        Course course = new Course();
        course.setId("course3_id");
        course.setName("Interface web");
        course.setCode("WEB1");
        course.setCredits(4);
        course.setTotalHours(24);
        course.setMainTeacher(TeacherIT.teacher1());
        return course;
    }

    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void user_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actualCourses = api
                .getCourses(null, null, null, null, null, null, null, null, null);

        assertEquals(3, actualCourses.size());
        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course2()));
        assertTrue(actualCourses.contains(course3()));
    }

    @Test
    void pagination_ok() throws ApiException{
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual1 = api
                .getCourses(null,null,null,null,null,null,null,null,null);
        List<Course> actual2= api
                .getCourses(2,1,null,null,null,null,null,null,null);
        List<Course> actual3 = api
                .getCourses(2,2,null,null,null,null,null,null,null);

        assertEquals(3,actual1.size());
        assertEquals(List.of(course1(),course2(),course3()),actual1);
        assertEquals(1,actual2.size());
        assertEquals(List.of(course2()),actual2);
        assertEquals(1,actual3.size());
        assertEquals(List.of(course3()),actual3);
    }

    @Test
    void user_read_with_filter_teacher_name_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual = api
                .getCourses(null, null, null, null, null, "one", null, null, null);

        assertEquals(2, actual.size());
        assertEquals(List.of(course1(),course3()), actual);
    }

    @Test
    void user_read_with_sort_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actualASC = api
                .getCourses(null, null, null, null, null, null, null, "ASC", "DESC");
        List<Course> actualDESC = api
                .getCourses(null, null, null, null, null, null, null, "DESC", "ASC");

        assertEquals(actualASC.size(), actualDESC.size());
        Collections.reverse(actualASC);
        assertEquals(actualDESC, actualASC);
    }

    @Test
    void user_read_with_sort_ko() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TeachingApi api = new TeachingApi(student1Client);

        assertThrowsApiException(
                "{\"type\":\"400 BAD_REQUEST\",\"message\":\"codeOrder value must be ASC or DESC\"}",
                () -> api.getCourses(null, null, null, null, null, null, null, "test", "test"));
    }

    @Test
    void user_read_with_filter_ok_1() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual = api
                .getCourses(1, 15, "prog", "aLgo", "6", "ONE", "tEa", "ASC", "DESC");
        assertEquals(1, actual.size());
        assertEquals(List.of(course1()), actual);
    }

    @Test
    void user_read_with_filter_ok_2() throws ApiException{
        ApiClient student2Client = anApiClient(STUDENT1_TOKEN);
        TeachingApi api =new TeachingApi(student2Client);

        List<Course> actual = api
                .getCourses(1,15,"test","test","5","ONE","tEa","ASC","DESC");
        assertEquals(0,actual.size());
        assertEquals(List.of(),actual);
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
