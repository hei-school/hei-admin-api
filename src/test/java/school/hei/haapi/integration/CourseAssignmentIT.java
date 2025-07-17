package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.FakeDataProvider.createAwardedCourse;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.NOT_EXISTING_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertListEquals;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

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
import school.hei.haapi.endpoint.rest.model.CrupdateCourseAssignment;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.integration.utils.CourseAssignmentUtils;
import school.hei.haapi.integration.utils.CourseUtils;
import school.hei.haapi.integration.utils.GroupUtils;
import school.hei.haapi.integration.utils.TeacherUtils;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;

@Testcontainers
@AutoConfigureMockMvc
class CourseAssignmentIT extends FacadeITMockedThirdParties {
  @Autowired private CourseAssignmentRepository courseAssignmentRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private UserRepository teacherRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private CourseAssignmentMapper courseAssignmentMapper;
  private Course courseProg1, courseProg2, courseProg3, courseProg4;
  private User teacherToky, teacherRyan, teacherLou;
  private Group groupG1, groupG2, groupG3;
  private school.hei.haapi.model.CourseAssignment assignProg1_toToky_forG1AndG2;
  private school.hei.haapi.model.CourseAssignment assignProg2_toToky_forG1AndG2;
  private school.hei.haapi.model.CourseAssignment assignProg2_toRyan_forG1AndG2;
  private school.hei.haapi.model.CourseAssignment assignProg3_toRyan_forG1AndG2;
  private school.hei.haapi.model.CourseAssignment assignProg4_toLou_forG3;
  private List<String> courseIds = new ArrayList<>();
  private List<String> teacherIds = new ArrayList<>();
  private List<String> groupIds = new ArrayList<>();
  private List<String> courseAssignmentIds = new ArrayList<>();

  private void setUpTestData() {
    courseProg1 = CourseUtils.prog1();
    courseProg2 = CourseUtils.prog2();
    courseProg3 = CourseUtils.prog3();
    courseProg4 = CourseUtils.prog4();
    teacherToky = TeacherUtils.toky();
    teacherRyan = TeacherUtils.ryan();
    teacherLou = TeacherUtils.lou();
    groupG1 = GroupUtils.g1();
    groupG2 = GroupUtils.g2();
    groupG3 = GroupUtils.g3();
    courseRepository.saveAll(List.of(courseProg1, courseProg2, courseProg3, courseProg4));
    teacherRepository.saveAll(List.of(teacherToky, teacherRyan, teacherLou));
    groupRepository.saveAll(List.of(groupG1, groupG2, groupG3));
    courseIds.addAll(
        List.of(
            courseProg1.getId(), courseProg2.getId(), courseProg3.getId(), courseProg4.getId()));
    teacherIds.addAll(List.of(teacherToky.getId(), teacherRyan.getId(), teacherLou.getId()));
    groupIds.addAll(List.of(groupG1.getId(), groupG2.getId(), groupG3.getId()));

    assignProg1_toToky_forG1AndG2 =
        CourseAssignmentUtils.createCourseAssignment(
            courseProg1, teacherToky, List.of(groupG1, groupG2));
    assignProg2_toToky_forG1AndG2 =
        CourseAssignmentUtils.createCourseAssignment(
            courseProg2, teacherToky, List.of(groupG1, groupG2));
    assignProg2_toRyan_forG1AndG2 =
        CourseAssignmentUtils.createCourseAssignment(
            courseProg2, teacherRyan, List.of(groupG1, groupG2));
    assignProg3_toRyan_forG1AndG2 =
        CourseAssignmentUtils.createCourseAssignment(
            courseProg3, teacherRyan, List.of(groupG1, groupG2));
    assignProg4_toLou_forG3 =
        CourseAssignmentUtils.createCourseAssignment(courseProg4, teacherLou, List.of(groupG3));
    courseAssignmentRepository.saveAll(
        List.of(
            assignProg1_toToky_forG1AndG2,
            assignProg2_toToky_forG1AndG2,
            assignProg3_toRyan_forG1AndG2,
            assignProg4_toLou_forG3));
    courseAssignmentIds.addAll(
        List.of(
            assignProg2_toToky_forG1AndG2.getId(),
            assignProg4_toLou_forG3.getId(),
            assignProg1_toToky_forG1AndG2.getId(),
            assignProg2_toRyan_forG1AndG2.getId(),
            assignProg3_toRyan_forG1AndG2.getId()));
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
    courseAssignmentRepository.deleteAllById(courseAssignmentIds);
    courseRepository.deleteAllById(courseIds);
    teacherRepository.deleteAllById(teacherIds);
    groupRepository.deleteAllById(groupIds);
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    CoursesApi api = new CoursesApi(manager1Client);

    List<CourseAssignment> courseAssignedToG1 =
        api.getCourseAssignmentsByGroupId(groupG1.getId(), 1, 10);
    List<CourseAssignment> allCourseAssignments =
        api.getCourseAssignmentsByCriteria(null, null, null, null, null);
    List<CourseAssignment> courseAssignedToLou =
        api.getCourseAssignmentByTeacherId(teacherLou.getId(), 1, 10);
    List<CourseAssignment> courseAssignedToToky =
        api.getCourseAssignmentByTeacherId(teacherToky.getId(), 1, 10);
    List<CourseAssignment> assignmentsForProg2 =
        api.getCourseAssignmentByCourseId(courseProg2.getId(), 1, 10);

    assertListEquals(
        courseAssignedToLou, courseAssignmentMapper.toRest(List.of(assignProg4_toLou_forG3)));
    assertListEquals(
        courseAssignedToToky,
        courseAssignmentMapper.toRest(
            List.of(assignProg1_toToky_forG1AndG2, assignProg2_toToky_forG1AndG2)));
    assertListEquals(
        assignmentsForProg2,
        courseAssignmentMapper.toRest(
            List.of(assignProg2_toRyan_forG1AndG2, assignProg2_toToky_forG1AndG2)));
    assertListEquals(
        courseAssignedToG1,
        courseAssignmentMapper.toRest(
            List.of(
                assignProg1_toToky_forG1AndG2,
                assignProg2_toToky_forG1AndG2,
                assignProg3_toRyan_forG1AndG2)));
    assertListEquals(
        allCourseAssignments,
        courseAssignmentMapper.toRest(
            List.of(
                assignProg1_toToky_forG1AndG2,
                assignProg2_toToky_forG1AndG2,
                assignProg2_toRyan_forG1AndG2,
                assignProg3_toRyan_forG1AndG2,
                assignProg4_toLou_forG3)));
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    CoursesApi api = new CoursesApi(student1Client);

    List<CourseAssignment> courseAssignmentsForToky =
        api.getCourseAssignmentByTeacherId(teacherToky.getId(), 1, 10);

    assertListEquals(
        courseAssignmentsForToky,
        courseAssignmentMapper.toRest(
            List.of(assignProg1_toToky_forG1AndG2, assignProg2_toToky_forG1AndG2)));
  }

  @Test
  void course_assignment_by_teacher_id_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    CoursesApi api = new CoursesApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"404 NOT_FOUND\",\"message\":\"User with id: "
            + NOT_EXISTING_ID
            + " not found\"}",
        () -> api.getCourseAssignmentByTeacherId(NOT_EXISTING_ID, 1, 10));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacherClient = anApiClient(TEACHER1_TOKEN);
    CoursesApi api = new CoursesApi(teacherClient);

    List<CourseAssignment> courseAssignedToG1 =
        api.getCourseAssignmentsByGroupId(groupG1.getId(), 1, 10);
    List<CourseAssignment> allCourseAssignments =
        api.getCourseAssignmentsByCriteria(null, null, null, null, null);
    List<CourseAssignment> courseAssignedToLou =
        api.getCourseAssignmentByTeacherId(teacherLou.getId(), 1, 10);
    List<CourseAssignment> courseAssignedToToky =
        api.getCourseAssignmentByTeacherId(teacherToky.getId(), 1, 10);
    List<CourseAssignment> assignmentsForProg2 =
        api.getCourseAssignmentByCourseId(courseProg2.getId(), 1, 10);

    assertListEquals(
        courseAssignedToLou, courseAssignmentMapper.toRest(List.of(assignProg4_toLou_forG3)));
    assertListEquals(
        courseAssignedToToky,
        courseAssignmentMapper.toRest(
            List.of(assignProg1_toToky_forG1AndG2, assignProg2_toToky_forG1AndG2)));
    assertListEquals(
        assignmentsForProg2,
        courseAssignmentMapper.toRest(
            List.of(assignProg2_toRyan_forG1AndG2, assignProg2_toToky_forG1AndG2)));
    assertListEquals(
        courseAssignedToG1,
        courseAssignmentMapper.toRest(
            List.of(
                assignProg1_toToky_forG1AndG2,
                assignProg2_toToky_forG1AndG2,
                assignProg3_toRyan_forG1AndG2)));
    assertListEquals(
        allCourseAssignments,
        courseAssignmentMapper.toRest(
            List.of(
                assignProg1_toToky_forG1AndG2,
                assignProg2_toToky_forG1AndG2,
                assignProg2_toRyan_forG1AndG2,
                assignProg3_toRyan_forG1AndG2,
                assignProg4_toLou_forG3)));
  }

  @Test
  void student_create_or_update_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    CoursesApi api = new CoursesApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () ->
            api.createOrUpdateCourseAssignmentsByCourseId(
                courseProg1.getId(), List.of(createAwardedCourse())));
  }

  @Test
  void teacher_create_or_update_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    CoursesApi api = new CoursesApi(teacher1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateCourseAssignments(List.of(createAwardedCourse())));

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateCourseAssignmentsByCourseId());
  }

  @Test
  void manager_create_or_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    CoursesApi api = new CoursesApi(manager1Client);
    List<CourseAssignment> allCourseAssignments =
        api.getCourseAssignmentsByCriteria(null, null, null, null, null).size();
    Course courseWeb1 = CourseUtils.web1();
    Course courseSys2 = CourseUtils.sys2();
    courseRepository.saveAll(List.of(courseWeb1, courseSys2));
    courseIds.addAll(List.of(courseWeb1.getId(), courseSys2.getId()));
    List<CrupdateCourseAssignment> toCreate =
        List.of(
            CourseAssignmentUtils.createCrupdateCourseAssignment(
                courseWeb1, teacherToky, List.of(groupG1, groupG2)),
            CourseAssignmentUtils.createCrupdateCourseAssignment(
                courseSys2, teacherLou, List.of(groupG1, groupG2)));
    courseAssignmentIds.addAll(List.of(toCreate.get(0).getId(), toCreate.get(1).getId()));

    List<CourseAssignment> createdCourseAssignments = api.createOrUpdateCourseAssignments(toCreate);
    List<CourseAssignment> afterCreateAndUpdate =
        api.getCourseAssignmentsByCriteria(null, null, null, null, null);

    assertEquals(
        afterCreateAndUpdate.size(), createdCourseAssignments.size() + allCourseAssignments.size());
  }

  // TODO: test update as manager
  @Test
  void manager_update_ok() throws ApiException {}
}
