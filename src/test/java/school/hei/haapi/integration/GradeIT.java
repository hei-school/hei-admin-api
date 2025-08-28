package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.AXEL_MONITOR_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.EXAM1_ID;
import static school.hei.haapi.integration.conf.TestUtils.EXAM3_ID;
import static school.hei.haapi.integration.conf.TestUtils.GROUP1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT3_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertBadRequestException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.test_data.CourseAssignmentTestData.createCourseAssignment;
import static school.hei.haapi.integration.test_data.CourseTestData.prog1;
import static school.hei.haapi.integration.test_data.CourseTestData.prog2;
import static school.hei.haapi.integration.test_data.ExamTestData.createExam;
import static school.hei.haapi.integration.test_data.GradeTestData.createRandomGrades;
import static school.hei.haapi.integration.test_data.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.test_data.GroupTestData.g1;
import static school.hei.haapi.integration.test_data.GroupTestData.g2;
import static school.hei.haapi.integration.test_data.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.test_data.MonitorTestData.monitorOfTolojanahary;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.test_data.TeacherTestData.toky;

import com.github.javafaker.Faker;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.casbin.casdoor.entity.CasdoorRole;
import org.casbin.casdoor.entity.CasdoorUser;
import org.casbin.casdoor.service.CasdoorAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.GradesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.CreateGrade;
import school.hei.haapi.endpoint.rest.model.Grade;
import school.hei.haapi.endpoint.rest.model.GradeHistory;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.endpoint.rest.model.UpdateGrade;
import school.hei.haapi.endpoint.rest.security.casdoorAuthentication.config.CertificateLoader;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.UserRepository;

@Testcontainers
@AutoConfigureMockMvc
class GradeIT extends FacadeITMockedThirdParties {
  @Autowired UserRepository userRepository;
  @Autowired GradeRepository gradeRepository;
  @Autowired CourseRepository courseRepository;
  @Autowired GroupFlowRepository groupFlowRepository;
  @Autowired GroupRepository groupRepository;
  @Autowired CourseAssignmentRepository courseAssignmentRepository;
  @Autowired MonitoringStudentRepository monitoringStudentRepository;
  @Autowired ExamRepository examRepository;
  @Autowired GradeMapper gradeMapper;
  private final Faker faker = new Faker();
  private User studentAxel;
  private User studentTolojanahary;
  private User monitorOfAxel;
  private User monitorOfTolojanahary;
  private Course courseProg1;
  private Course courseProg2;
  private User teacherToky;
  private Exam exam1Prog1;
  private Exam exam2Prog1;
  private CourseAssignment assign_prog1_toToky_forGroup1;
  private CourseAssignment assign_prog2_toToky_forGroup2;
  private Group groupG1;
  private Group groupG2;
  private List<school.hei.haapi.model.Grade> gradesExam1Prog1;
  private List<school.hei.haapi.model.Grade> gradesExam2Prog1;
  private school.hei.haapi.model.Grade studentAxelGradeExam1Prog1;
  private school.hei.haapi.model.Grade studentTolojanaharyGradeExam1Prog1;
  private school.hei.haapi.model.Grade studentAxelGradeExam2Prog1;
  private GroupFlow groupFlowsAxel;
  private GroupFlow groupFlowsTolojanahary;
  private List<String> studentIds = new ArrayList<>();
  private List<String> teacherIds = new ArrayList<>();
  private List<String> groupIds = new ArrayList<>();
  private List<String> courseIds = new ArrayList<>();
  private List<String> courseAssignmentIds = new ArrayList<>();
  private List<String> examIds = new ArrayList<>();
  private List<String> gradeIds = new ArrayList<>();
  private List<String> groupFlowIds = new ArrayList<>();

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private void setUpTestData() {
    groupG1 = g1();
    groupG2 = g2();
    studentAxel = axel();
    studentTolojanahary = tolojanahary();
    courseProg1 = prog1();
    courseProg2 = prog2();
    teacherToky = toky();
    assign_prog1_toToky_forGroup1 =
        createCourseAssignment(courseProg1, teacherToky, List.of(groupG1));
    assign_prog2_toToky_forGroup2 =
        createCourseAssignment(courseProg2, teacherToky, List.of(groupG2));

    monitorOfAxel = monitorOfAxel();
    monitorOfTolojanahary = monitorOfTolojanahary();
    studentAxel.setMonitors(List.of(monitorOfAxel));
    studentTolojanahary.setMonitors(List.of(monitorOfTolojanahary));
    groupFlowsAxel = createGroupFlow(studentAxel, groupG1);
    groupFlowsTolojanahary = createGroupFlow(studentTolojanahary, groupG1);

    exam1Prog1 = createExam(Instant.parse("2025-07-22T10:15:30Z"), assign_prog1_toToky_forGroup1);
    exam2Prog1 = createExam(Instant.parse("2025-09-22T10:15:30Z"), assign_prog1_toToky_forGroup1);
    gradesExam1Prog1 = createRandomGrades(List.of(studentAxel, studentTolojanahary), exam1Prog1);
    gradesExam2Prog1 = createRandomGrades(List.of(studentAxel), exam2Prog1);
    studentAxelGradeExam1Prog1 = gradesExam1Prog1.get(0);
    studentTolojanaharyGradeExam1Prog1 = gradesExam1Prog1.get(1);
    studentAxelGradeExam2Prog1 = gradesExam2Prog1.getFirst();

    groupRepository.saveAll(List.of(groupG1, groupG2));
    userRepository.saveAll(List.of(monitorOfAxel, monitorOfTolojanahary));
    userRepository.saveAll(List.of(studentAxel, studentTolojanahary));
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitorOfAxel.getId(), studentAxel.getId());
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitorOfTolojanahary.getId(), studentTolojanahary.getId());
    userRepository.saveAll(List.of(teacherToky));
    courseRepository.saveAll(List.of(courseProg1, courseProg2));
    groupFlowRepository.saveAll(List.of(groupFlowsAxel, groupFlowsTolojanahary));
    courseAssignmentRepository.saveAll(
        List.of(assign_prog1_toToky_forGroup1, assign_prog2_toToky_forGroup2));
    examRepository.save(exam1Prog1);
    examRepository.save(exam2Prog1);
    exam1Prog1.setGrades(gradesExam1Prog1);
    exam2Prog1.setGrades(gradesExam2Prog1);
    gradeRepository.saveAll(gradesExam1Prog1);
    gradeRepository.saveAll(gradesExam2Prog1);

    groupIds.addAll(List.of(groupG1.getId(), groupG2.getId()));
    studentIds.addAll(List.of(studentAxel.getId(), studentTolojanahary.getId()));
    groupFlowIds.addAll(List.of(groupFlowsAxel.getId(), groupFlowsTolojanahary.getId()));
    teacherIds.add(teacherToky.getId());
    courseIds.addAll(List.of(courseProg1.getId(), courseProg2.getId()));
    courseAssignmentIds.add(assign_prog1_toToky_forGroup1.getId());
    examIds.add(exam1Prog1.getId());
    gradeIds.addAll(List.of(gradesExam1Prog1.get(0).getId(), gradesExam1Prog1.get(1).getId()));
    gradeIds.add(gradesExam2Prog1.getFirst().getId());
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
    setUpTestData();
  }

  @Test
  void student_read_other_grade_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    GradesApi api = new GradesApi(student1Client);
    assertThrowsForbiddenException(() -> api.getGradesByStudentId(STUDENT2_ID, 1, 10));
    assertThrowsForbiddenException(() -> api.getParticipantGrade(GROUP1_ID, EXAM1_ID));
  }

  @Test
  void manager_crupdate_invalid_grade_ko() {
    ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
    GradesApi api = new GradesApi(managerClient);
    CreateGrade newGrade = new CreateGrade();
    newGrade.setScore(28.2);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"score must be between 0 and 20\"}",
        () -> api.createParticipantGrade(EXAM1_ID, STUDENT3_ID, newGrade));
  }

  @Test
  void manager_crupdate_grade_invalid_student_ko() {
    GradesApi api = new GradesApi(anApiClient(MANAGER1_TOKEN));
    CreateGrade newGrade = new CreateGrade();
    newGrade.setScore(18.2);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Student with id "
            + STUDENT3_ID
            + " is not in exam "
            + EXAM3_ID
            + "\"}",
        () -> api.createParticipantGrade(EXAM3_ID, STUDENT3_ID, newGrade));
  }

  @Test
  void student_crupdate_grade_forbidden() {
    ApiClient studentClient = anApiClient(STUDENT1_TOKEN);
    GradesApi api = new GradesApi(studentClient);

    UpdateGrade updateGrade = new UpdateGrade();
    updateGrade.setComment("Rectification");
    updateGrade.setStudentRef(student1().getRef());
    updateGrade.setGrade(new CreateGrade().score(90.0));

    assertThrowsForbiddenException(
        () -> api.correctParticipantGrade(EXAM1_ID, STUDENT1_ID, updateGrade));
  }

  @Test
  void student_get_all_grade_ko() {
    GradesApi studentApi = new GradesApi(anApiClient(STUDENT1_TOKEN));
    assertThrowsForbiddenException(() -> studentApi.getStudentGradesForExam(EXAM1_ID, 1, 10));
  }

  @Test
  void teacher_or_manager_or_admin_get_course_grades_ok() throws ApiException {
    GradesApi managerApi = new GradesApi(anApiClient(MANAGER1_TOKEN));
    GradesApi adminApi = new GradesApi(anApiClient(ADMIN1_TOKEN));
    GradesApi teacherApi = new GradesApi(anApiClient(TEACHER1_TOKEN));

    List<Grade> adminAxelGrades =
        adminApi.getCourseGrades(studentAxel.getId(), courseProg1.getId(), null, null, null);
    List<Grade> managerAxelGrades =
        managerApi.getCourseGrades(studentAxel.getId(), courseProg1.getId(), null, null, null);
    List<Grade> teacherAxelGrades =
        teacherApi.getCourseGrades(studentAxel.getId(), courseProg1.getId(), null, null, null);

    assertGradeExists(adminAxelGrades, studentAxelGradeExam1Prog1);
    assertGradeExists(adminAxelGrades, studentAxelGradeExam2Prog1);
    assertGradeExists(managerAxelGrades, studentAxelGradeExam1Prog1);
    assertGradeExists(managerAxelGrades, studentAxelGradeExam2Prog1);
    assertGradeExists(teacherAxelGrades, studentAxelGradeExam1Prog1);
    assertGradeExists(teacherAxelGrades, studentAxelGradeExam2Prog1);
  }

  @Test
  void manager_read_by_student_id_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    GradesApi api = new GradesApi(manager1Client);
    List<Grade> axelGrades = api.getGradesByStudentId(studentAxel.getId(), 1, 10);

    assertGradeExists(axelGrades, studentAxelGradeExam1Prog1);
    assertGradeExists(axelGrades, studentAxelGradeExam2Prog1);
  }

  @Test
  void student_get_course_grades_ko() {
    GradesApi studentApi = new GradesApi(anApiClient(STUDENT1_TOKEN));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () ->
            studentApi.getCourseGrades(studentAxel.getId(), courseProg1.getId(), null, null, null));
  }

  @Test
  void get_grades_for_student_with_unassigned_course_ko() {
    setUpCasdoorMonitor(casdoorAuthServiceMock, certificateLoaderMock, monitorOfAxel);
    GradesApi monitorApi = new GradesApi(anApiClient(AXEL_MONITOR_TOKEN));

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Student's current group is not assigned to course with id: %s\"}"
            .formatted(courseProg2.getId()),
        () ->
            monitorApi.getCourseGrades(studentAxel.getId(), courseProg2.getId(), null, null, null));
  }

  @Test
  void monitor_get_grades_of_other_student_ko() {
    setUpCasdoorMonitor(casdoorAuthServiceMock, certificateLoaderMock, monitorOfAxel);
    GradesApi monitorApi = new GradesApi(anApiClient(AXEL_MONITOR_TOKEN));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () ->
            monitorApi.getCourseGrades(
                studentTolojanahary.getId(), courseProg1.getId(), null, null, null));
  }

  @Test
  void monitor_get_own_grades_ok() throws ApiException {
    setUpCasdoorMonitor(casdoorAuthServiceMock, certificateLoaderMock, monitorOfAxel);
    GradesApi monitorApi = new GradesApi(anApiClient(AXEL_MONITOR_TOKEN));
    List<Grade> axelGrades =
        monitorApi.getCourseGrades(studentAxel.getId(), courseProg1.getId(), null, null, null);

    assertGradeExists(axelGrades, studentAxelGradeExam1Prog1);
    assertGradeExists(axelGrades, studentAxelGradeExam2Prog1);
  }

  @Test
  void monitor_get_own_yearly_result_ok() throws ApiException {
    setUpCasdoorMonitor(casdoorAuthServiceMock, certificateLoaderMock, monitorOfAxel);
    GradesApi monitorApi = new GradesApi(anApiClient(AXEL_MONITOR_TOKEN));
    assertDoesNotThrow(() -> monitorApi.getYearlyResult(studentAxel.getId(), L1));
  }

  @Test
  void monitor_get_other_yearly_result_ko() throws ApiException {
    setUpCasdoorMonitor(casdoorAuthServiceMock, certificateLoaderMock, monitorOfAxel);
    GradesApi monitorApi = new GradesApi(anApiClient(AXEL_MONITOR_TOKEN));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> monitorApi.getYearlyResult(studentTolojanahary.getId(), L1));
  }

  @Test
  void monitor_get_own_yearly_result_transcript_ok() {
    setUpCasdoorMonitor(casdoorAuthServiceMock, certificateLoaderMock, monitorOfAxel);
    GradesApi monitorApi = new GradesApi(anApiClient(AXEL_MONITOR_TOKEN));

    assertBadRequestException(
        "Cannot generate transcript for this level. This level is not yet completed",
        () -> {
          monitorApi.getYearlyResultTranscript(studentAxel.getId(), L1);
        });
  }

  @Test
  void monitor_get_else_yearly_result_transcript_ko() {
    setUpCasdoorMonitor(casdoorAuthServiceMock, certificateLoaderMock, monitorOfAxel);
    GradesApi monitorApi = new GradesApi(anApiClient(AXEL_MONITOR_TOKEN));

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> monitorApi.getYearlyResultTranscript(studentTolojanahary.getId(), L1));
  }

  @Test
  void manager_get_any_yearly_result() {
    GradesApi managerApi = new GradesApi(anApiClient(MANAGER1_TOKEN));

    assertBadRequestException(
        "Cannot generate transcript for this level. This level is not yet completed",
        () -> {
          managerApi.getYearlyResultTranscript(studentAxel.getId(), L1);
        });
  }

  @Test
  void monitor_get_exam_own_grades_ok() throws ApiException {
    setUpCasdoorMonitor(casdoorAuthServiceMock, certificateLoaderMock, monitorOfAxel);
    GradesApi monitorApi = new GradesApi(anApiClient(AXEL_MONITOR_TOKEN));
    Grade axelGrades = monitorApi.getParticipantGrade(exam1Prog1.getId(), studentAxel.getId());

    Grade actual = gradeMapper.toRest(studentAxelGradeExam1Prog1);

    axelGrades
        .createdAt(null)
        .updateDate(null)
        .getExam()
        .getCourseAssignment()
        .getGroups()
        .forEach(group -> group.setCreationDatetime(null));
    actual
        .createdAt(null)
        .updateDate(null)
        .getExam()
        .getCourseAssignment()
        .getGroups()
        .forEach(group -> group.setCreationDatetime(null));
    assertEquals(axelGrades, actual);
  }

  @Test
  void create_existing_grade_ko() {
    GradesApi gradesApi = new GradesApi(anApiClient(MANAGER1_TOKEN));
    String axelId = studentAxel.getId();
    String examId = exam1Prog1.getId();
    List<CreateGrade> createGrades = List.of(new CreateGrade().score(10.).studentId(axelId));

    assertBadRequestException(
        "Grade for the student %s for the exam %s already exist".formatted(axelId, examId),
        () -> gradesApi.createParticipantsGradeForExam(examId, createGrades));
  }

  @Test
  void create_grade_ok() throws ApiException {
    var groupRandomG3 = g1();
    var studentRandomAxel = axel();
    var courseRandomProg3 = prog1();
    var teacherRandomToky = toky();
    var assign_prog3_toTeacherRandom_forGroup3 =
        createCourseAssignment(courseRandomProg3, teacherRandomToky, List.of(groupRandomG3));

    var monitorOfRandomStudent = monitorOfAxel();
    studentRandomAxel.setMonitors(List.of(monitorOfRandomStudent));
    var groupFlowsRandomAxel = createGroupFlow(studentRandomAxel, groupRandomG3);

    var exam1RandomProg3 =
        createExam(Instant.parse("2025-07-22T10:15:30Z"), assign_prog3_toTeacherRandom_forGroup3);

    groupRepository.saveAll(List.of(groupRandomG3));
    userRepository.saveAll(List.of(monitorOfRandomStudent));
    userRepository.saveAll(List.of(studentRandomAxel));
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitorOfRandomStudent.getId(), studentRandomAxel.getId());
    userRepository.saveAll(List.of(teacherRandomToky));
    courseRepository.saveAll(List.of(courseRandomProg3));
    groupFlowRepository.saveAll(List.of(groupFlowsRandomAxel));
    courseAssignmentRepository.saveAll(List.of(assign_prog3_toTeacherRandom_forGroup3));
    examRepository.save(exam1RandomProg3);

    groupIds.add(groupRandomG3.getId());
    studentIds.add(studentRandomAxel.getId());
    groupFlowIds.add(groupFlowsRandomAxel.getId());
    teacherIds.add(teacherRandomToky.getId());
    courseIds.add(courseRandomProg3.getId());
    courseAssignmentIds.add(assign_prog3_toTeacherRandom_forGroup3.getId());
    examIds.add(exam1RandomProg3.getId());

    GradesApi gradesApi = new GradesApi(anApiClient(MANAGER1_TOKEN));
    String axelId = studentRandomAxel.getId();
    String examId = exam1RandomProg3.getId();
    CreateGrade createGrade = new CreateGrade().score(10.).studentId(axelId);
    List<CreateGrade> createGrades = List.of(createGrade);

    List<StudentGrade> createdGrades =
        gradesApi.createParticipantsGradeForExam(examId, createGrades);

    assertEquals(1, createdGrades.size());
    StudentGrade createdGrade = createdGrades.getFirst();
    assertEquals(axelId, createdGrade.getStudent().getId());
    assertEquals(examId, createdGrade.getGrade().getExam().getId());
    assertEquals(createGrade.getScore(), createdGrade.getGrade().getScore());
  }

  @Test
  void monitor_get_grade_history_ok() throws ApiException {
    GradesApi gradesApi = new GradesApi(anApiClient(MANAGER1_TOKEN));
    var initialGradesAxelExam1Prog1 = gradeRepository.save(gradesExam1Prog1.getFirst());
    var firstModificationGrade = faker.number().randomDouble(0, 20, 2);
    var firstModification = initialGradesAxelExam1Prog1.toBuilder().build();
    firstModification.setScore(firstModificationGrade, faker.lorem().paragraph(2));
    firstModification = gradeRepository.save(firstModification);
    var secondModificationGrade = faker.number().randomDouble(0, 20, 2);
    var secondModification = firstModification.toBuilder().build();
    secondModification.setScore(secondModificationGrade, faker.lorem().paragraph(2));
    gradeRepository.save(secondModification);

    List<GradeHistory> gradeHistory =
        gradesApi.getOrderedGradeHistory(
            initialGradesAxelExam1Prog1.getId(), null, null, null, null, null);

    assertEquals(2, gradeHistory.size());
    assertEquals(firstModificationGrade, gradeHistory.getFirst().getScore());
    assertEquals(secondModificationGrade, gradeHistory.get(1).getScore());
  }

  private void setUpCasdoorMonitor(
      CasdoorAuthService casdoorAuthService, CertificateLoader certificateLoader, User monitor) {
    given(certificateLoader.getCertificate()).willReturn("mocked-certificate");
    when(casdoorAuthService.parseJwtToken(AXEL_MONITOR_TOKEN))
        .thenReturn(getCasdoorUserFromMonitor(monitor));
  }

  private CasdoorUser getCasdoorUserFromMonitor(User monitor) {
    CasdoorUser user = new CasdoorUser();
    user.setEmail(monitor.getEmail());
    CasdoorRole casdoorRole = new CasdoorRole();
    casdoorRole.setOwner("dummy");
    casdoorRole.setName("student");
    String[] roleUsers = List.of("dummy/user").toArray(new String[0]);
    casdoorRole.setUsers(roleUsers);
    user.setRoles(List.of(casdoorRole));

    return user;
  }

  private void assertGradeExists(List<Grade> grades, school.hei.haapi.model.Grade expectedEntity) {
    Grade expected = gradeMapper.toRest(expectedEntity);
    assertTrue(
        grades.stream()
            .anyMatch(
                grade -> {
                  if (!expected.getId().equals(grade.getId())) return false;

                  return expected.getExam().getId().equals(grade.getExam().getId())
                      && grade.getScore().equals(expected.getScore());
                }));
  }
}
