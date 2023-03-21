package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class CourseIT {
    @MockBean
    private SentryConf sentryConf;
    @MockBean
    private CognitoComponent cognitoComponentMock;
    @MockBean
    private EventBridgeClient eventBridgeClientMock;


    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, CourseIT.ContextInitializer.SERVER_PORT);
    }
    public static Course course1(){
        Course course = new Course();
        course.setId("course1_id");
        course.setCode("PROG1");
        course.setName("Algo");
        course.setCredits(100);
        course.setMain_teacher(User.Role.TEACHER);
        return course;
    }
    public static Course course2(){
        Course course = new Course();
        course.setId("course2_id");
        course.setCode("WEB1");
        course.setName("Interface web");
        course.setCredits(100);
        course.setMain_teacher(User.Role.TEACHER);
        return course;
    }
    static Course createCourse(){
        return Course.builder()
                .id("course3_id")
                .code("SYS1")
                .name("Systeme d'exploitation")
                .credits(100)
                .main_teacher(User.Role.TEACHER)
                .build();
    }
    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
        setUpEventBridge(eventBridgeClientMock);
    }

    @Test
    void course_write_update_if_exist_ok () throws ApiException {
        List<Course> toUpdate = List.of(course1(),course2());

        assertTrue(toUpdate.contains(course1()));
        assertTrue(toUpdate.contains(course2()));
    }
    @Test
    void course_write_create_if_not_exist_ok () throws ApiException {
        List<Course> toUpdate = List.of(course1(),course2(),createCourse());
        Course toCreate = createCourse();

        Course expected = Course.builder()
                .id("course3_id")
                .code("SYS1")
                .name("Systeme d'exploitation")
                .credits(100)
                .main_teacher(User.Role.TEACHER)
                .build();
        assertEquals(expected,toCreate);
        assertTrue(toUpdate.contains(createCourse()));
    }
    @Test
    void course_write_update_if_exist_ko () throws ApiException{
        Course toUpdate= (Course) List.of();
        assertThrowsApiException("{\"type\":\"404 NOT_FOUND\",\"message\":null}", () -> List.of(toUpdate) );
    }
    @Test
    void course_write_create_if_exist_ko () throws ApiException{
        Course toCreate= createCourse();
        assertThrowsApiException("{\"type\":\"500 INTERNAL_SERVER_ERROR\",\"message\":null}", () -> List.of(toCreate) );
    }
    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
