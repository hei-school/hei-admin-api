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
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;
import java.time.LocalDate;
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
        return TestUtils.anApiClient(token, CourseIT.ContextInitializer.SERVER_PORT);
    }

    public static Teacher teacher1() {
        Teacher teacher = new Teacher();
        teacher.setId("teacher1_id");
        teacher.setFirstName("One");
        teacher.setLastName("Teacher");
        teacher.setEmail("test+teacher1@hei.school");
        teacher.setRef("TCR21001");
        teacher.setPhone("0322411125");
        teacher.setStatus(EnableStatus.ENABLED);
        teacher.setSex(Teacher.SexEnum.F);
        teacher.setBirthDate(LocalDate.parse("1990-01-01"));
        teacher.setEntranceDatetime(Instant.parse("2021-10-08T08:27:24.00Z"));
        teacher.setAddress("Adr 3");
        return teacher;
    }

    public static Teacher teacher2() {
        Teacher teacher = new Teacher();
        teacher.setId("teacher2_id");
        teacher.setFirstName("Two");
        teacher.setLastName("Teacher");
        teacher.setEmail("test+teacher2@hei.school");
        teacher.setRef("TCR21002");
        teacher.setPhone("0322411126");
        teacher.setStatus(EnableStatus.ENABLED);
        teacher.setSex(Teacher.SexEnum.M);
        teacher.setBirthDate(LocalDate.parse("1990-01-02"));
        teacher.setEntranceDatetime(Instant.parse("2021-10-09T08:28:24Z"));
        teacher.setAddress("Adr 4");
        return teacher;
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

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
