package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.FakeDataProvider.createAwardedCourse;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCreatableCreateAwardedCourseList;
import static school.hei.haapi.integration.conf.TestUtils.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.CoursesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseAssignment;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;

@Testcontainers
@AutoConfigureMockMvc
class AwardedCourseIT extends FacadeITMockedThirdParties {
  private List<Course> allCourses() {
    return List.of(
            new Course().id("1").code("PROG1").name("Algorithmique").credits(6).totalHours(80),
            new Course().id("2").code("PROG2").name("Programmation Orientée-Objet").credits(10).totalHours(60),
            new Course().id("3").code("PROG3").name("Implémentation d'API Backend").credits(6).totalHours(80)
    );
  }

  private List<Group> allGroups() {
    return List.of(
            new Group().id("1").name("G1").ref("G1").size(30).attributedColor("green"),
            new Group().id("2").name("G2").ref("G2").size(30).attributedColor("blue"),
            new Group().id("3").name("G3").ref("G3").size(20).attributedColor("red")
    );
  }
  
  private List<Teacher> allTeachers() {
    return List.of(
            new Teacher().id("1").ref("REF-TEACHER-001").firstName("Lou").lastName("Andria").email("john.smith@hei.school").phone("+261 34 12 345 01").status(EnableStatus.ENABLED),
            new Teacher().id("2").ref("REF-TEACHER-002").firstName("Ryan").lastName("Andria").email("jane.doe@hei.school").phone("+261 34 12 345 02").status(EnableStatus.ENABLED),
            new Teacher().id("3").ref("REF-TEACHER-003").firstName("Toky").lastName("Ramarozaka").email("bob.wilson@hei.school").phone("+261 34 12 345 03").status(EnableStatus.DISABLED)
    );
  }

  private CourseAssignment assign_PROG1_to_Lou_for_G1_and_G2 () {
    return new CourseAssignment()
            .id("prog1-toky-g1g2")
            .course(allCourses().get(0))
            .mainTeacher(allTeachers().get(0))
            .groups(List.of(allGroups().get(0), allGroups().get(1)));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    CoursesApi api = new CoursesApi(manager1Client);

    List<CourseAssignment> courseAssignedToG1 = api.getAllCourseAssignmentsByGroupId(GROUP1_ID, 1, 10);
    List<CourseAssignment> allAwardedCourse =
        api.getAllCourseAssignmentsByCriteria(null, null, null, null, null);
    List<CourseAssignment> awardedCoursesByTeacher =
        api.getCourseAssignmentByTeacherId(TEACHER1_ID, 1, 10);
    List<CourseAssignment> awardedCoursesByCourse =
        api.getAllAwardedCourseByCriteria(null, null, COURSE1_ID, null, null);

    assertEquals(awardedCourse1(), actual);

    assertEquals(3, courseAssignedToG1.size());
    assertTrue(courseAssignedToG1.contains(awardedCourse1()));
    assertTrue(courseAssignedToG1.contains(awardedCourse2()));
    assertTrue(courseAssignedToG1.contains(awardedCourse4()));

    assertEquals(5, allAwardedCourse.size());
    assertTrue(allAwardedCourse.contains(awardedCourse1()));
    assertTrue(allAwardedCourse.contains(awardedCourse2()));
    assertTrue(allAwardedCourse.contains(awardedCourse3()));
    assertTrue(allAwardedCourse.contains(awardedCourse4()));

    assertEquals(1, awardedCoursesByTeacher.size());
    assertTrue(awardedCoursesByTeacher.contains(awardedCourse1()));

    assertEquals(3, awardedCoursesByCourse.size());
    assertTrue(awardedCoursesByCourse.contains(awardedCourse1()));
    assertTrue(awardedCoursesByCourse.contains(awardedCourse2()));
    assertTrue(awardedCoursesByCourse.contains(awardedCourse3()));

    assertEquals(3, awardedCoursesAssignedToTeacher.size());
    assertTrue(awardedCoursesAssignedToTeacher.contains(awardedCourse2()));
    assertTrue(awardedCoursesAssignedToTeacher.contains(awardedCourse3()));
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    CoursesApi api = new CoursesApi(student1Client);

    List<CourseAssignment> awardedCoursesAssignedToTeacher =
        api.getAwardedCoursesAssignedToTeacher(TEACHER2_ID, 1, 10);

    assertEquals(3, awardedCoursesAssignedToTeacher.size());
    assertTrue(awardedCoursesAssignedToTeacher.contains(awardedCourse2()));
    assertTrue(awardedCoursesAssignedToTeacher.contains(awardedCourse3()));
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    CoursesApi api = new CoursesApi(student1Client);
    assertThrowsForbiddenException(
        () -> api.getAwardedCoursesByIdAndGroupId(GROUP1_ID, AWARDED_COURSE1_ID));
    assertThrowsForbiddenException(() -> api.getAllAwardedCourseByGroup(GROUP1_ID, 1, 10));
  }

  @Test
  void awarded_courses_by_teacher_id_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    CoursesApi api = new CoursesApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"404 NOT_FOUND\",\"message\":\"User with id: "
            + NOT_EXISTING_ID
            + " not found\"}",
        () -> api.getAwardedCoursesAssignedToTeacher(NOT_EXISTING_ID, 1, 10));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    CoursesApi api = new CoursesApi(teacher1Client);
    CourseAssignment actual = api.getAwardedCoursesByIdAndGroupId(GROUP1_ID, AWARDED_COURSE1_ID);

    List<CourseAssignment> actuals = api.getAllAwardedCourseByGroup(GROUP1_ID, 1, 10);

    List<CourseAssignment> allAwardedCourse =
        api.getAllAwardedCourseByCriteria(null, null, null, null, null);

    List<CourseAssignment> awardedCoursesByTeacher =
        api.getAllAwardedCourseByCriteria(TEACHER1_ID, null, null, null, null);

    List<CourseAssignment> awardedCoursesByCourse =
        api.getAllAwardedCourseByCriteria(null, null, COURSE1_ID, null, null);

    List<CourseAssignment> awardedCoursesAssignedToTeacher =
        api.getAwardedCoursesAssignedToTeacher(TEACHER2_ID, 1, 10);

    assertEquals(awardedCourse1(), actual);

    assertEquals(3, actuals.size());
    assertTrue(actuals.contains(awardedCourse1()));
    assertTrue(actuals.contains(awardedCourse2()));
    assertTrue(actuals.contains(awardedCourse4()));

    assertEquals(5, allAwardedCourse.size());
    assertTrue(allAwardedCourse.contains(awardedCourse1()));
    assertTrue(allAwardedCourse.contains(awardedCourse2()));
    assertTrue(allAwardedCourse.contains(awardedCourse3()));
    assertTrue(allAwardedCourse.contains(awardedCourse4()));

    assertEquals(1, awardedCoursesByTeacher.size());
    assertTrue(awardedCoursesByTeacher.contains(awardedCourse1()));

    assertEquals(3, awardedCoursesByCourse.size());
    assertTrue(awardedCoursesByCourse.contains(awardedCourse1()));
    assertTrue(awardedCoursesByCourse.contains(awardedCourse2()));
    assertTrue(awardedCoursesByCourse.contains(awardedCourse3()));

    assertEquals(3, awardedCoursesAssignedToTeacher.size());
    assertTrue(awardedCoursesAssignedToTeacher.contains(awardedCourse2()));
    assertTrue(awardedCoursesAssignedToTeacher.contains(awardedCourse3()));
  }

  @Test
  void student_create_or_update_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    CoursesApi api = new CoursesApi(student1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateAwardedCourses(GROUP1_ID, List.of(createAwardedCourse())));

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () ->
            api.createOrUpdateAwardedCoursesAssignToTeacher(
                TEACHER2_ID, someAwardedCoursesToCrupdate()));
  }

  @Test
  void teacher_create_or_update_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    CoursesApi api = new CoursesApi(teacher1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateAwardedCourses(GROUP1_ID, List.of(createAwardedCourse())));

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () ->
            api.createOrUpdateAwardedCoursesAssignToTeacher(
                TEACHER2_ID, someAwardedCoursesToCrupdate()));
  }

  @Test
  void manager_create_or_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    CoursesApi api = new CoursesApi(manager1Client);
    int numberOfExamToAdd = 3;
    List<CourseAssignment> actualCreatList =
        api.createOrUpdateAwardedCourses(
            GROUP1_ID, someCreatableCreateAwardedCourseList(numberOfExamToAdd));
    assertEquals(numberOfExamToAdd, actualCreatList.size());

    List<CourseAssignment> awardedCoursesUpdated =
        api.createOrUpdateAwardedCoursesAssignToTeacher(
            TEACHER2_ID, someAwardedCoursesToCrupdate());

    assertTrue(awardedCoursesUpdated.contains(updatedAwardedCourse2()));
    assertEquals(2, awardedCoursesUpdated.size());
  }
}