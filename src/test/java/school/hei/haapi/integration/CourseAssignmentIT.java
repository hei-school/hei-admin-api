package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.NOT_EXISTING_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertCourseAssignmentsIgnoringGroupCreationDateTime;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.test_data.CourseAssignmentTestData.createCourseAssignment;
import static school.hei.haapi.integration.test_data.CourseAssignmentTestData.createCrupdateCourseAssignment;
import static school.hei.haapi.integration.test_data.CourseTestData.prog1;
import static school.hei.haapi.integration.test_data.CourseTestData.prog2;
import static school.hei.haapi.integration.test_data.CourseTestData.prog4;
import static school.hei.haapi.integration.test_data.GroupTestData.g1;
import static school.hei.haapi.integration.test_data.GroupTestData.g2;
import static school.hei.haapi.integration.test_data.TeacherTestData.ryan;
import static school.hei.haapi.integration.test_data.TeacherTestData.toky;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.CoursesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.CourseAssignmentMapper;
import school.hei.haapi.endpoint.rest.model.CourseAssignment;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.FakeDataProvider;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;

class CourseAssignmentIT extends FacadeITMockedThirdParties {
  @Autowired private CourseAssignmentRepository courseAssignmentRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private UserRepository teacherRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private CourseAssignmentMapper courseAssignmentMapper;
  private Course courseProg1, courseProg2;
  private User teacherToky, teacherRyan;
  private Group groupG1, groupG2;
  private school.hei.haapi.model.CourseAssignment assignProg1_toToky_forG1AndG2;
  private school.hei.haapi.model.CourseAssignment assignProg2_toToky_forG1;
  private school.hei.haapi.model.CourseAssignment assignProg2_toRyan_forG2;
  private List<String> courseIds = new ArrayList<>();
  private List<String> teacherIds = new ArrayList<>();
  private List<String> groupIds = new ArrayList<>();
  private List<String> courseAssignmentIds = new ArrayList<>();

  private void setUpTestData() {
    courseProg1 = prog1();
    courseProg2 = prog2();
    teacherToky = toky();
    teacherRyan = ryan();
    groupG1 = g1();
    groupG2 = g2();
    assignProg1_toToky_forG1AndG2 =
        createCourseAssignment(courseProg1, teacherToky, List.of(groupG1, groupG2));
    assignProg2_toToky_forG1 = createCourseAssignment(courseProg2, teacherToky, List.of(groupG1));
    assignProg2_toRyan_forG2 = createCourseAssignment(courseProg2, teacherRyan, List.of(groupG2));

    courseRepository.saveAll(List.of(courseProg1, courseProg2));
    teacherRepository.saveAll(List.of(teacherToky, teacherRyan));
    groupRepository.saveAll(List.of(groupG1, groupG2));
    courseAssignmentRepository.saveAll(
        List.of(assignProg1_toToky_forG1AndG2, assignProg2_toToky_forG1, assignProg2_toRyan_forG2));

    courseIds.addAll(List.of(courseProg1.getId(), courseProg2.getId()));
    teacherIds.addAll(List.of(teacherToky.getId(), teacherRyan.getId()));
    groupIds.addAll(List.of(groupG1.getId(), groupG2.getId()));
    courseAssignmentIds.addAll(
        List.of(
            assignProg2_toToky_forG1.getId(),
            assignProg1_toToky_forG1AndG2.getId(),
            assignProg2_toRyan_forG2.getId()));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
    setUpTestData();
  }

  @AfterEach
  void tearDown() {
    courseRepository.deleteAllById(courseIds);
    teacherRepository.deleteAllById(teacherIds);
    groupRepository.deleteAllById(groupIds);
    courseAssignmentRepository.deleteAllById(courseAssignmentIds);
    courseAssignmentIds = new ArrayList<>();
    courseIds = new ArrayList<>();
    teacherIds = new ArrayList<>();
    groupIds = new ArrayList<>();
  }

  private void assertCourseAssignments(
      String method,
      String id,
      List<school.hei.haapi.model.CourseAssignment> expected,
      CoursesApi api)
      throws ApiException {
    assertRestCourseAssignments(
        method, id, expected.stream().map(courseAssignmentMapper::toRest).toList(), api);
  }

  private void assertRestCourseAssignments(
      String method, String id, List<CourseAssignment> expected, CoursesApi api)
      throws ApiException {
    List<CourseAssignment> actual =
        switch (method) {
          case "byTeacherId" -> api.getCourseAssignmentByTeacherId(id, 1, 10);
          case "byCourseId" -> api.getCourseAssignmentsByCourseId(id, 1, 10);
          case "byGroupId" -> api.getCourseAssignmentsByGroupId(id, 1, 10);
          default -> api.getCourseAssignmentsByCriteria(null, null, null, null, null);
        };
    assertCourseAssignmentsIgnoringGroupCreationDateTime(actual, expected);
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    CoursesApi api = new CoursesApi(student1Client);

    assertCourseAssignments(
        "byTeacherId",
        teacherToky.getId(),
        List.of(assignProg1_toToky_forG1AndG2, assignProg2_toToky_forG1),
        api);
  }

  @Test
  void manager_read_with_bad_id_ko() {
    ApiClient teacher1Client = anApiClient(MANAGER1_TOKEN);
    CoursesApi api = new CoursesApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"404 NOT_FOUND\",\"message\":\"Teacher with id: "
            + NOT_EXISTING_ID
            + " not found\"}",
        () -> api.getCourseAssignmentByTeacherId(NOT_EXISTING_ID, 1, 10));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacherClient = anApiClient(TEACHER1_TOKEN);
    CoursesApi api = new CoursesApi(teacherClient);

    assertCourseAssignments(
        "byGroupId",
        groupG1.getId(),
        List.of(assignProg1_toToky_forG1AndG2, assignProg2_toToky_forG1),
        api);
    assertCourseAssignments(
        "all",
        null,
        List.of(assignProg1_toToky_forG1AndG2, assignProg2_toToky_forG1, assignProg2_toRyan_forG2),
        api);
    assertCourseAssignments(
        "byTeacherId",
        teacherToky.getId(),
        List.of(assignProg1_toToky_forG1AndG2, assignProg2_toToky_forG1),
        api);
    assertCourseAssignments(
        "byCourseId",
        courseProg2.getId(),
        List.of(assignProg2_toToky_forG1, assignProg2_toRyan_forG2),
        api);
  }

  @Test
  void student_create_or_update_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    CoursesApi api = new CoursesApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () ->
            api.createOrUpdateCourseAssignmentsByCourseId(
                courseProg1.getId(), List.of(FakeDataProvider.createCourseAssignment())));
  }

  @Test
  void teacher_create_or_update_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    CoursesApi api = new CoursesApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () ->
            api.createOrUpdateCourseAssignments(
                List.of(FakeDataProvider.createCourseAssignment())));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateCourseAssignmentsByCourseId(courseProg1.getId(), List.of()));
  }

  @Test
  void manager_create_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    CoursesApi api = new CoursesApi(manager1Client);
    Course courseProg4 = prog4();
    courseRepository.save(courseProg4);
    courseIds.add(courseProg4.getId());
    var toCreate =
        createCrupdateCourseAssignment(courseProg4, teacherToky, List.of(groupG1, groupG2));
    int initialSize = api.getCourseAssignmentsByCriteria(null, null, null, null, null).size();

    List<CourseAssignment> created = api.createOrUpdateCourseAssignments(List.of(toCreate));
    courseAssignmentIds.add(created.getFirst().getId());

    assertEquals(
        initialSize + 1,
        api.getCourseAssignmentsByCriteria(null, null, null, null, null).size(),
        "List have not been extended by the new element");
    assertRestCourseAssignments("byTeacherId", teacherToky.getId(), created, api);
  }

  @Test
  void manager_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    CoursesApi api = new CoursesApi(manager1Client);

    var toUpdate =
        createCrupdateCourseAssignment(courseProg1, teacherRyan, List.of(groupG1, groupG2));
    List<CourseAssignment> updatedRestCourseAssignment =
        api.createOrUpdateCourseAssignments(List.of(toUpdate));
    courseAssignmentIds.remove(assignProg1_toToky_forG1AndG2.getId());
    courseAssignmentIds.add(updatedRestCourseAssignment.getFirst().getId());

    assertRestCourseAssignments("byTeacher", teacherRyan.getId(), updatedRestCourseAssignment, api);
  }
}
