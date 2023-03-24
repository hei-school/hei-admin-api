package school.hei.haapi.integration;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.client.api.TeachingApi;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.response.CreateCourses;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.UserService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @MockBean
    private EventBridgeClient eventBridgeClientMock;
    @Autowired
    private static UserService userService;
    @Autowired
    private static CourseMapper courseMapper;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, StudentIT.ContextInitializer.SERVER_PORT);
    }

    public static Course course1() {
        Course course = new Course();
        course.setId("course1_id");
        course.setCode("PROG1");
        course.setName("Algorithmique");
        course.setCredits(6);
        course.setTotal_hours(360);
        course.setMain_teacher(userService.getById(TEACHER1_ID));
        return course;
    }

    public static Course course2() {
        Course course = new Course();
        course.setId("course2_id");
        course.setCode("PROG3");
        course.setName("P.O.O avancée");
        course.setCredits(6);
        course.setTotal_hours(360);
        course.setMain_teacher(userService.getById(TEACHER2_ID));
        return course;
    }

    public static CreateCourses someCreatableCourse() {
        CreateCourses course = new CreateCourses();
        course.setId("string");
        course.setCode("CODE");
        course.setName("some name");
        course.setCredits(0);
        course.setTotal_hours(0);
        course.setMain_teacher_id(TEACHER1_ID);
        return course;
    }

    static List<Course> someCreatableCourseList(int nbOfCourse) {
        List<Course> courseList = new ArrayList<>();
        for (int i = 0; i < nbOfCourse; i++) {
            courseList.add(courseMapper.toDomain(someCreatableCourse()));
        }
        return courseList;
    }
    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
        setUpEventBridge(eventBridgeClientMock);
    }
    @Test
    void read_course_by_code_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual = api.getCourses(1,15,"PROG1",null,null,null,null);

        assertTrue(actual.contains(course1()));
    }
    @Test
    void read_course_by_credit_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual = api.getCourses(1,15,null,null,6,null,null);

        assertTrue(actual.contains(course1()));
        assertTrue(actual.contains(course2()));
    }

    @Test
    void read_course_by_name_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual = api.getCourses(1,15,null,"Algorithmiques",null,null,null);

        assertTrue(actual.contains(course1()));
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
