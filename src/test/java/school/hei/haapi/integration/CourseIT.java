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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

    static List<school.hei.haapi.endpoint.rest.model.Course> courses (){
        school.hei.haapi.endpoint.rest.model.Course course2 = new school.hei.haapi.endpoint.rest.model.Course();
        course2.setId("course_id2");
        course2.setCode("WEB1");
        course2.setName("IHM");
        course2.setCredits(50);
        course2.setTotalHours(50);
        course2.setMainTeacher(teacher1());

        school.hei.haapi.endpoint.rest.model.Course course1 = new school.hei.haapi.endpoint.rest.model.Course();
        course1.setId("course_id1");
        course1.setCode("PROG1");
        course1.setName("programmation");
        course1.setCredits(50);
        course1.setTotalHours(50);
        course1.setMainTeacher(teacher1());

        List<school.hei.haapi.endpoint.rest.model.Course> courseList = new ArrayList<>();
        courseList.add(course1);
        courseList.add(course2);

        return courseList;
    }


    @BeforeEach
    void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void course_read_ok() throws ApiException {

        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        TeachingApi api = new TeachingApi(student1Client);

        List<school.hei.haapi.endpoint.rest.model.Course> actual1 = api.getCourses(1,5);

        assertNotEquals(courses(), actual1);
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }

}

