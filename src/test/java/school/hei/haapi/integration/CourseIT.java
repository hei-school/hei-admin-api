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
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
class CourseIT {

    @MockBean
    private CognitoComponent cognitoComponentMock;

    @MockBean
    private EventBridgeClient eventBridgeClientMock;

    private static ApiClient anApiClient() {
        return TestUtils.anApiClient(TestUtils.STUDENT1_TOKEN, CourseIT.ContextInitializer.SERVER_PORT);
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

    static Course course1 (){
        Course course1=new Course();
        course1.setId("course_id1");
        course1.setCode("PROG1");
        course1.setName("programmation");
        course1.setCredits(50);
        course1.setTotalHours(50);
        course1.setMainTeacher(teacher1());
        return course1;
    }

    static Course course2 (){
        Course course2=new Course();
        course2.setId("course_id2");
        course2.setCode("WEB1");
        course2.setName("IHM");
        course2.setCredits(50);
        return course2;
    }

    public static Course course3() {
        Course course3 = new Course();
        course3.setId("course_id3");
        course3.setCode("DB1");
        course3.setName("bases de données");
        course3.setCredits(50);
        return course3;
    }

    public static Course course4() {
        Course course4 = new Course();
        course4.setId("course_id4");
        course4.setCode("AI1");
        course4.setName("intelligence artificielle");
        course4.setCredits(75);
        return course4;
    }

    public static Course course5() {
        Course course5 = new Course();
        course5.setId("course_id5");
        course5.setCode("SE1");
        course5.setName("ingénierie logicielle");
        course5.setCredits(60);
        return course5;
    }


    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
        setUpEventBridge(eventBridgeClientMock);
    }

    @Test
    void get_all_course_without_any_params() throws ApiException {

        ApiClient student1Client = anApiClient();

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual1 = api.getCourses(1,6,null,null,null,null,null,null,null);

        assertEquals(5, actual1.size());
    }

    @Test
    void get_all_course_by_code() throws ApiException {

        ApiClient student1Client = anApiClient();

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual1 = api.getCourses(1,6,null,null,"DB",null,null,null,null);

        assertEquals(course3(), actual1.get(0));
    }

    @Test
    void get_all_course_by_name() throws ApiException {

        ApiClient student1Client = anApiClient();

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual1 = api.getCourses(1,6,null,null,null,"ingénierie",null,null,null);

        assertEquals(course5(), actual1.get(0));
    }

    @Test
    void get_all_course_by_credits() throws ApiException {

        ApiClient student1Client = anApiClient();

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual1 = api.getCourses(1,6,null,null,null,null,75,null,null);

        assertEquals(course4(), actual1.get(0));
    }

    @Test
    void get_all_course_by_teacher_first_name() throws ApiException {

        ApiClient student1Client = anApiClient();

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual1 = api.getCourses(1,6,null,null,null,null,null,"One",null);

        assertEquals(course2(), actual1.get(0));
    }


    @Test
    void get_all_course_by_teacher_last_name() throws ApiException {

        ApiClient student1Client = anApiClient();

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual1 = api.getCourses(1,6,null,null,null,null,null,null,"Teacher");

        List<Course> expected = new ArrayList<>();
        expected.add(course1());
        expected.add(course2());

        assertEquals(expected, actual1);
    }

    @Test
    public void test_Course_In_ASC_credits_order() throws ApiException {

        ApiClient student1Client = anApiClient();

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual = api.getCourses(1,6,"ASC",null,null,null,null,null,null);

        assertEquals(course5(), actual.get(3));
        assertEquals(course4(), actual.get(4));
    }

    @Test
    public void test_Course_In_DESC_credits_order() throws ApiException {

        ApiClient student1Client = anApiClient();

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual = api.getCourses(1,6,"DESC",null,null,null,null,null,null);

        assertEquals(course4(), actual.get(0));
        assertEquals(course5(), actual.get(1));
    }

    @Test
    public void test_Course_In_ASC_code_order() throws ApiException {

        ApiClient student1Client = anApiClient();

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual = api.getCourses(1,6,null,"ASC",null,null,null,null,null);

        assertEquals(course4(), actual.get(0));
        assertEquals(course3(), actual.get(1));
    }

    @Test
    public void test_Course_In_DESC_code_order() throws ApiException {

        ApiClient student1Client = anApiClient();

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual = api.getCourses(1,6,null,"DESC",null,null,null,null,null);

        assertEquals(course2(), actual.get(0));
        assertEquals(course5(), actual.get(1));
    }

    @Test
    void get_all_course_with_all_params() throws ApiException {

        ApiClient student1Client = anApiClient();

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actual1 = api.getCourses(1,6,null,null,"AI","intelligence",75,null,null);

        assertEquals(course4(), actual1.get(0));
    }
    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }

}

