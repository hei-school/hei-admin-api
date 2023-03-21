package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.model.StudentCourse;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.StudentIT.student2;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;


import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.COURSE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE3_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = GroupIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class CourseIT {
    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, GroupIT.ContextInitializer.SERVER_PORT);
    }

    public static Course course1(){
        Course course = new Course();
        course.setId(COURSE1_ID);
        course.setCode("PROG1");
        course.setName("Algorithmics");
        course.setCredits(3);
        course.setTotalHours(40);
        course.setMainTeacher(TeacherIT.teacher1());
        return course;
    }

    public static Course course2(){
        Course course = new Course();
        course.setId(COURSE2_ID);
        course.setCode("WEB1");
        course.setName(null);
        course.setCredits(2);
        course.setTotalHours(36);
        course.setMainTeacher(TeacherIT.teacher1());
        return course;
    }
    public static Course course3(){
        Course course = new Course();
        course.setId(COURSE3_ID);
        course.setCode("MGT1");
        course.setName("name");
        course.setCredits(2);
        course.setTotalHours(20);
        course.setMainTeacher(TeacherIT.teacher2());
        return course;
    }
    @BeforeEach
    void setUp() {
        setUpCognito(cognitoComponentMock);
    }


    @Test
    void manager_read_student_courses_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        UsersApi api = new UsersApi(manager1Client);

        List<Course> expectCours = new ArrayList<>();
        expectCours.add(0,course1());
        List<Course> actualCours = api.getStudentCoursesById(STUDENT1_ID, CourseStatus.LINKED);
        assertEquals(actualCours, expectCours);
    }

    @Test
    void read_student_courses_unliked_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        UsersApi api = new UsersApi(manager1Client);

        List<Course> expectCours = new ArrayList<>();
        expectCours.add(0,course2());
        List<Course> actualCours = api.getStudentCoursesById(STUDENT1_ID, CourseStatus.UNLINKED);
        assertEquals(actualCours, expectCours);
    }

    @Test
    void read_student_courses_whith_status_null_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        UsersApi api = new UsersApi(manager1Client);

        List<Course> expectCours = new ArrayList<>();
        expectCours.add(0,course1());
        List<Course> actualCours = api.getStudentCoursesById(STUDENT1_ID, null);
        assertEquals(actualCours, expectCours);
    }

    @Test
    void teacher_read_student_courses_ko() throws ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        UsersApi api = new UsersApi(teacher1Client);

        assertThrowsApiException(
                "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                () -> api.getStudentCoursesById(STUDENT1_ID, CourseStatus.LINKED));
        assertThrowsApiException(
                "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                () -> api.getStudentCoursesById(STUDENT1_ID, null));
        assertThrowsApiException(
                "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                () -> api.getStudentCoursesById(STUDENT1_ID, CourseStatus.UNLINKED));
    }

    @Test
    void student_read_courses_ko() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        UsersApi api = new UsersApi(student1Client);

        assertThrowsApiException(
                "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                () -> api.getStudentCoursesById(STUDENT2_ID, CourseStatus.LINKED));
        assertThrowsApiException(
                "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                () -> api.getStudentCoursesById(STUDENT2_ID, null));
        assertThrowsApiException(
                "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                () -> api.getStudentCoursesById(STUDENT2_ID, CourseStatus.UNLINKED));
    }


}
