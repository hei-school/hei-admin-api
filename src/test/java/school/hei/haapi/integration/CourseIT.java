package school.hei.haapi.integration;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.SortOrder;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;


@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CourseIT.ContextInitializer.class)
@AutoConfigureMockMvc
@Slf4j
class CourseIT {
    private static final String TEACHER1_FIRST_NAME_PART = "On";
    private static final String TEACHER1_FIRST_NAME_PART_INSENSITIVE_CASE = "ONe";
    private static final String TEACHER2_LAST_NAME_PART_INSENSITIVE_CASE = "TeaChER";
    @MockBean
    private SentryConf sentryConf;
    @MockBean
    private CognitoComponent cognitoComponentMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, CourseIT.ContextInitializer.SERVER_PORT);
    }


    public static Course course1() {
        Course course = new Course();
        course.setId("course1_id");
        course.setName("Algorithmique");
        course.setCode("PROG1");
        course.setCredits(6);
        course.setTotalHours(1);
        course.setMainTeacher(teacher1());
        return course;
    }

    public static Course course2() {
        Course course = new Course();
        course.setId("course2_id");
        course.setName("POO avancée");
        course.setCode("PROG3");
        course.setCredits(6);
        course.setTotalHours(2);
        course.setMainTeacher(teacher2());
        return course;
    }


    public static Course course3() {
        Course course = new Course();
        course.setId("course3_id");
        course.setName("Interface web");
        course.setCode("WEB1");
        course.setCredits(4);
        course.setTotalHours(1);
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

    public static Teacher teacher1() {
        Teacher teacher = new Teacher();
        teacher.setId("teacher1_id");
        teacher.setRef("TCR21001");
        teacher.setFirstName("One");
        teacher.setLastName("Teacher");
        teacher.setSex(Teacher.SexEnum.F);
        teacher.setBirthDate(LocalDate.parse("1990-01-01"));
        teacher.setAddress("Adr 3");
        teacher.setPhone("0322411125");
        teacher.setEmail("test+teacher1@hei.school");
        teacher.setEntranceDatetime(Instant.parse("2021-10-08T08:27:24Z"));
        teacher.setStatus(EnableStatus.ENABLED);
        return teacher;
    }


    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    public static UpdateStudentCourse someModifiableCourse() {
        return new UpdateStudentCourse()
                .courseId("course1_id")
                .status(CourseStatus.LINKED);
    }

    @Test
    void teacher_write_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        UsersApi api = new UsersApi(manager1Client);
        List<Course> actual = api.updateStudentCourses(STUDENT1_ID, List.of(someModifiableCourse()));
        assertTrue(actual.contains(course1()));
    }

    @Test
    void bad_token_write_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);

        TeachingApi api = new TeachingApi(anonymousClient);
        assertThrowsForbiddenException(() -> api.crupdateCourses(List.of(new CrupdateCourse())));
    }


    @Test
    void filter_ordered_courses_by_code() throws ApiException {
        ApiClient student1Client = anApiClient(TEACHER1_TOKEN);

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actualCourses1 = api.getAllCourses(
                "PROG1",
                null,
                null,
                null,
                null,
                SortOrder.ASC,
                null,
                1,
                15
        );
        assertEquals(actualCourses1.get(0).getCode(),(course1().getCode()));
    }

    @Test
    void filter_courses_by_code() throws ApiException {
        ApiClient student1Client = anApiClient(TEACHER1_TOKEN);

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actualCourses1 = api.getAllCourses(
                null,
                "Algorithmique",
                null,
                null,
                null,
                SortOrder.ASC,
                null,
                1,
                15
        );
        assertEquals(actualCourses1.get(0).getName(),(course1().getName()));
    }

    @Test
    void filter_courses_by_teacher_name_insensitive_case() throws ApiException {
        ApiClient student1Client = anApiClient(TEACHER1_TOKEN);

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actualCourses1 = api.getAllCourses(
                null,
                null,
                null,
                teacher1().getFirstName(),
                null,
                SortOrder.ASC,
                null,
                1,
                15
        );
        List<Course> actualCourses2 = api.getAllCourses(
                null,
                null,
                null,
                null,
                "Tea",
                SortOrder.ASC,
                null,
                1,
                15
        );
        List<Course> actualCourses3 = api.getAllCourses(
                null,
                null,
                null,
                "On",
                "Tea",
                SortOrder.ASC,
                null,
                1,
                15
        );
        assertEquals(3, actualCourses1.size());
        assertEquals(3, actualCourses2.size());
        assertEquals(2, actualCourses3.size());
    }

    @Test
    void sort_test() throws ApiException {
        ApiClient student1Client = anApiClient(TEACHER1_TOKEN);

        TeachingApi api = new TeachingApi(student1Client);

        List<Course> actualCourses1 = api.getAllCourses(
                null,
                null,
                null,
                null,
                null,
                SortOrder.ASC,
                null,
                1,
                15
        );
        List<Course> actualCourses2 = api.getAllCourses(
                null,
                null,
                null,
                null,
                null,
                SortOrder.DESC,
                null,
                1,
                15
        );
        List<Course> actualCourses3 = api.getAllCourses(
                null,
                null,
                null,
                null,
                null,
                SortOrder.ASC,
                SortOrder.DESC,
                1,
                15
        );

        assertEquals(actualCourses1.get(0),course3());
        assertEquals(actualCourses2.get(0),course1());
        assertEquals(actualCourses3.get(0),course3());

    }

    @Test
    void student_write_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        UsersApi api = new UsersApi(student1Client);

        assertThrowsApiException(
                "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                () -> api.updateStudentCourses(STUDENT1_ID, List.of()));


        TeachingApi teacherApi = new TeachingApi(student1Client);
        assertThrowsForbiddenException(() -> teacherApi.crupdateCourses(List.of(new CrupdateCourse())));
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
    void manager_write_with_some_bad_fields_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        TeachingApi api = new TeachingApi(teacher1Client);
        assertThrowsForbiddenException(() -> api.crupdateCourses(List.of(new CrupdateCourse())));
    }

//    @Test
//    void manager_write_create_ok() throws ApiException {
//        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
//        CrupdateCourse toCreate = someCreatableCourse();
//        List<CrupdateCourse> toCreateList = List.of(toCreate);
//
//        TeachingApi api = new TeachingApi(manager1Client);
//        Course created = api.crupdateCourses(toCreateList).get(0);
//
//        assertTrue(isValidUUID(created.getId()));
//        toCreate.setId(created.getId());
//        assertNotNull(created.getTotalHours());
//        assertEquals(toCreate.getCode(), created.getCode());
//    }

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
}