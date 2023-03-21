package school.hei.haapi.integration;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Courses;
import school.hei.haapi.model.User;


import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CoursesIT.ContextInitializer.class)
@AutoConfigureMockMvc

public class CoursesIT {
    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, GroupIT.ContextInitializer.SERVER_PORT);
    }

    public static Courses course1() {
        Courses course = new Courses();
        User teacher1 = new User();
        course.setId("course1_id");
        course.setCode("courseCode1");
        course.setName("Name of course one");
        course.setCredits(1);
        course.setTotalHours(1);
        course.setMainTeacher(teacher1);
        return course;
    }

    public static Courses course2() {
        Courses course = new Courses();
        User teacher2 = new User();
        course.setId("course2_id");
        course.setCode("courseCode2");
        course.setName("Name of course two");
        course.setCredits(2);
        course.setTotalHours(2);
        course.setMainTeacher(teacher2);
        return course;
    }
    //createsometable peut-etre

    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void

}
