package school.hei.haapi.integration;


import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = FeeIT.ContextInitializer.class)
@AutoConfigureMockMvc
class CourseIT {
    @MockBean
    private SentryConf sentryConf;
    @MockBean
    private CognitoComponent cognitoComponentMock;
    private static CourseStatus status;
    private static UserMapper userMapper;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, FeeIT.ContextInitializer.SERVER_PORT);
    }

    public static Student student1() {
        Student student = new Student();
        student.setId("student1_id");
        student.setFirstName("Ryan");
        student.setLastName("Andria");
        student.setEmail("test+ryan@hei.school");
        student.setRef("STD21001");
        student.setPhone("0322411123");
        student.setStatus(EnableStatus.ENABLED);
        student.setSex(Student.SexEnum.M);
        student.setBirthDate(LocalDate.parse("2000-01-01"));
        student.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
        student.setAddress("Adr 1");
        return student;
    }

    public static Student student2() {
        Student student = new Student();
        student.setId("student2_id");
        student.setFirstName("Two");
        student.setLastName("Student");
        student.setEmail("test+student2@hei.school");
        student.setRef("STD21002");
        student.setPhone("0322411124");
        student.setStatus(EnableStatus.ENABLED);
        student.setSex(Student.SexEnum.F);
        student.setBirthDate(LocalDate.parse("2000-01-02"));
        student.setEntranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"));
        student.setAddress("Adr 2");
        return student;
    }




    public static Course course1() {
        Course course = new Course();
        course.setId("course1_id");
        course.setName("Name of course one");
        course.setCode("CRS21001");
        course.setCredits(1);
        course.setTotalHours(1);
        course.setMainTeacher(teacher1());
        return course;
    }

    public static Course course2() {
        Course course = new Course();
        course.setId("course2_id");
        course.setName("Name of course two");
        course.setCode("CRS21002");
        course.setCredits(2);
        course.setTotalHours(2);
        course.setMainTeacher(teacher1());
        return course;
    }

    public static CrupdateCourse someCreatableCourse() {
        CrupdateCourse course = new CrupdateCourse();
        course.setName("Some name");
        course.setCode("GRP21-" + randomUUID());
        course.setCredits(3);
        course.setTotalHours(3);
        course.setMainTeacherId(TEACHER1_ID);
        return course;
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
        course.setName("CRS21001");
       course.setMainTeacher(teacher1());
       return course;
}
   
    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        UsersApi api = new UsersApi(student1Client);

        Course actualCourse = api.getStudentCoursesById(STUDENT1_ID, CourseStatus.LINKED);
        assertEquals(course1(),actualCourse);
    }
    public static UpdateStudentCourse someModifiableCourse(){
        return  new  UpdateStudentCourse()
                .courseId("course1_id")
                .status(CourseStatus.LINKED);
    }
    @Test
    void teacher_write_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        UsersApi api = new UsersApi(manager1Client);
        List<Course> actual = api.updateStudentCourses(STUDENT1_ID, List.of(someModifiableCourse()));
        assertEquals(actual,course1());
         }
    void badtoken_write_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);

        TeachingApi api = new TeachingApi(anonymousClient);
        assertThrowsForbiddenException(() -> api.crupdateCourses(List.of(new CrupdateCourse())));
    }

    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        TeachingApi api = new TeachingApi(student1Client);
        List<Course> actualCourses = api.getCourses(1, 15);

        assertTrue(actualCourses.contains(course1()));
        assertTrue(actualCourses.contains(course2()));
    }

    @Test
    void student_write_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        UsersApi api = new UsersApi(student1Client);

        assertThrowsApiException(
                "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                () -> api.updateStudentCourses(STUDENT1_ID, List.of()));


        TeachingApi api = new TeachingApi(student1Client);
        assertThrowsForbiddenException(() -> api.crupdateCourses(List.of(new CrupdateCourse())));
    }

    @Test
    void teacher_write_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        UsersApi api = new UsersApi(student1Client);

        assertThrowsApiException(
                "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                () -> api.updateStudentCourses(STUDENT1_ID, List.of()));
    }

    @Test
    void manager_write_with_some_bad_fields_ko() throws ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        TeachingApi api = new TeachingApi(teacher1Client);
        assertThrowsForbiddenException(() -> api.crupdateCourses(List.of(new CrupdateCourse())));
    }

    @Test
    void manager_write_create_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        CrupdateCourse toCreate = someCreatableCourse();
        List<CrupdateCourse> toCreateList = List.of(toCreate);

        TeachingApi api = new TeachingApi(manager1Client);
        Course created = api.crupdateCourses(toCreateList).get(0);

        assertTrue(isValidUUID(created.getId()));
        toCreate.setId(created.getId());
        assertNotNull(created.getTotalHours());
        assertEquals(toCreate.getCode(),created.getCode());
    }

    @Test
    void manager_write_update_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);
        CrupdateCourse toUpdate = someCreatableCourse();
        List<Course> toUpdateCourse = api.crupdateCourses(List.of(toUpdate));
        toUpdate.setId(toUpdateCourse.get(0).getId());
        toUpdate.setName("A new name zero");

        Course updated = api.crupdateCourses(List.of(toUpdate)).get(0);

        assertEquals(updated.getName(), toUpdate.getName());
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }