package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseSortIT.ContextInitializer.class)
@AutoConfigureMockMvc
class CourseSortIT{
    @MockBean
    private CognitoComponent cognitoComponentMock;

    @MockBean
    private EventBridgeClient eventBridgeClientMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, CourseSortIT.ContextInitializer.SERVER_PORT);
    }
    static Course course1 (){
        Course course1=new Course();
        course1.setId("1");
        course1.setCode("PROG1");
        course1.setName("programmation");
        course1.setCredits(6);
        course1.setTotalHours(50);
        course1.setMainTeacher(teacher1());
        return course1;
    }

    static Course course2 (){
        Course course2=new Course();
        course2.setId("2");
        course2.setCode("PROG3");
        course2.setName("P.O.O avancée");
        course2.setCredits(7);
        course2.setTotalHours(50);
        course2.setMainTeacher(teacher2());
        return course2;
    }

    public static Course course3() {
        Course course3 = new Course();
        course3.setId("3");
        course3.setCode("WEB1");
        course3.setName("Interface web");
        course3.setCredits(4);
        course3.setTotalHours(50);
        course3.setMainTeacher(teacher1());
        return course3;
    }

    public static Teacher teacher1() {
        Teacher teacher = new Teacher();
        teacher.setId("1");
        teacher.setFirstName("Tokimahery");
        teacher.setLastName("Ramarozaka");
        return teacher;
    }

    public static Teacher teacher2() {
        Teacher teacher = new Teacher();
        teacher.setId("2");
        teacher.setFirstName("Ryan");
        teacher.setLastName("Andriamahery");
        return teacher;
    }

    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
        setUpEventBridge(eventBridgeClientMock);
    }

    @Test
    public void test_Course_In_Order() throws ApiException {

        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual = api.getCourses(1,5,"DESC","ASC");

        assertEquals(course2(), actual.get(0));
        assertEquals(course3(), actual.get(1));
        assertEquals(course1(), actual.get(2));
        assertEquals(course3(), actual.get(0));
        assertEquals(course1(), actual.get(1));
        assertEquals(course2(), actual.get(2));

    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();
        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }

}
