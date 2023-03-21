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
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.TeacherIT.teacher1;
import static school.hei.haapi.integration.TeacherIT.teacher2;
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
        return TestUtils.anApiClient(token, CourseIT.ContextInitializer.SERVER_PORT);
    }

    public static Course course1(){
        Course course = new Course();
        course.setId("course1_id");
        course.setCode("PROG1");
        course.setName("Algo");
        course.setCredits(3);
        course.setTotalHours(30);
        course.setMainTeacher(teacher1());
        return course;
    }

    public static Course course2(){
        Course course = new Course();
        course.setId("course2_id");
        course.setCode("PROG3");
        course.setName("Ops");
        course.setCredits(2);
        course.setTotalHours(20);
        course.setMainTeacher(teacher2());
        return course;
    }

    public static CrupdateCourse toCreateSuccess() {
        CrupdateCourse toCreate = new CrupdateCourse();
        toCreate.setId(null);
        toCreate.setCode("CR_2");
        toCreate.setName("Course_02");
        toCreate.setCredits(10);
        toCreate.setTotalHours(10);
        toCreate.setMainTeacherId(teacher1().getId());
        return toCreate;
    }

    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }


    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actualCourses = api.getCourses(1,10);

        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course2()));
    }

    @Test
    void teacher_read_ok() throws ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        TeachingApi api = new TeachingApi(teacher1Client);

        List<Course> actualCourses = api.getCourses(1,10);

        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course2()));
    }

    @Test
    void manager_read_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);

        List<Course> actualCourses = api.getCourses(1,10);

        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course2()));
    }

    @Test
    void manager_write_create_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);
        List<Course> created = api.crupdateCourses(List.of(toCreateSuccess()));

        assertEquals(1, created.size());
        Course created1 = created.get(0);
        assertTrue(isValidUUID(created1.getId()));
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
