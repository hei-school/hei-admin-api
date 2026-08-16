package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.integration.conf.ApiAssertions.assertBadRequestException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsApiException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.CourseAssignmentTestData.createCourseAssignment;
import static school.hei.haapi.integration.testData.CourseTestData.prog1;
import static school.hei.haapi.integration.testData.CourseTestData.prog2;
import static school.hei.haapi.integration.testData.ExamTestData.createExam;
import static school.hei.haapi.integration.testData.GradeTestData.createRandomGrades;
import static school.hei.haapi.integration.testData.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.testData.GroupTestData.g1;
import static school.hei.haapi.integration.testData.GroupTestData.g2;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfTolojanahary;
import static school.hei.haapi.integration.testData.StaffTestData.adminMialy;
import static school.hei.haapi.integration.testData.StudentResultOverviewTestData.promotionH;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;
import static school.hei.haapi.integration.testData.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;
import static school.hei.haapi.model.dto.MonitorStudentLinkDto.Status.LINKED;

import com.github.javafaker.Faker;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.rest.api.GradesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.CreateGrade;
import school.hei.haapi.endpoint.rest.model.UpdateGrade;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
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
import school.hei.haapi.service.PromotionService;

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
  @MockBean private EventProducer eventProducer;
  @MockBean private PromotionService promotionService;

  private final Faker faker = new Faker();

  private User studentAxel;
  private User studentTolojanahary;

  /** In G2, which carries no exam: reaches the "student is not in exam" check. */
  private User studentInOtherGroup;

  private User monitorOfAxel;
  private User monitorOfTolojanahary;
  private User teacherToky;
  private User managerHasina;
  private User adminUser;

  private Course courseProg1;
  private Course courseProg2;
  private Group groupG1;
  private Group groupG2;
  private CourseAssignment assignProg1ToTokyForG1;
  private CourseAssignment assignProg2ToTokyForG2;
  private Exam exam1Prog1;
  private Exam exam2Prog1;
  private List<Grade> gradesExam1Prog1;
  private List<Grade> gradesExam2Prog1;
  private Grade axelGradeExam1Prog1;
  private Grade axelGradeExam2Prog1;
  private GroupFlow groupFlowAxel;
  private GroupFlow groupFlowTolojanahary;
  private GroupFlow groupFlowOtherGroup;

  private final List<String> studentIds = new ArrayList<>();
  private final List<String> teacherIds = new ArrayList<>();
  private final List<String> groupIds = new ArrayList<>();
  private final List<String> courseIds = new ArrayList<>();
  private final List<String> courseAssignmentIds = new ArrayList<>();
  private final List<String> examIds = new ArrayList<>();
  private final List<String> groupFlowIds = new ArrayList<>();

  private String axelToken;
  private String managerToken;
  private String adminToken;
  private String teacherToken;
  private String axelMonitorToken;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private void setUpTestData() {
    groupG1 = g1();
    groupG2 = g2();
    studentAxel = axel();
    studentTolojanahary = tolojanahary();
    studentInOtherGroup = freddy();
    courseProg1 = prog1();
    courseProg2 = prog2();
    teacherToky = toky();
    monitorOfAxel = monitorOfAxel();
    monitorOfTolojanahary = monitorOfTolojanahary();

    assignProg1ToTokyForG1 = createCourseAssignment(courseProg1, teacherToky, List.of(groupG1));
    assignProg2ToTokyForG2 = createCourseAssignment(courseProg2, teacherToky, List.of(groupG2));

    groupFlowAxel = createGroupFlow(studentAxel, groupG1);
    groupFlowTolojanahary = createGroupFlow(studentTolojanahary, groupG1);
    groupFlowOtherGroup = createGroupFlow(studentInOtherGroup, groupG2);

    exam1Prog1 = createExam(Instant.parse("2025-07-22T10:15:30Z"), assignProg1ToTokyForG1);
    exam2Prog1 = createExam(Instant.parse("2025-09-22T10:15:30Z"), assignProg1ToTokyForG1);
    gradesExam1Prog1 = createRandomGrades(List.of(studentAxel, studentTolojanahary), exam1Prog1);
    gradesExam2Prog1 = createRandomGrades(List.of(studentAxel), exam2Prog1);
    axelGradeExam1Prog1 = gradesExam1Prog1.getFirst();
    axelGradeExam2Prog1 = gradesExam2Prog1.getFirst();

    groupRepository.saveAll(List.of(groupG1, groupG2));
    userRepository.saveAll(List.of(monitorOfAxel, monitorOfTolojanahary));
    userRepository.saveAll(List.of(studentAxel, studentTolojanahary, studentInOtherGroup));
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitorOfAxel.getId(), List.of(studentAxel.getId()), LINKED.toString());
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitorOfTolojanahary.getId(), List.of(studentTolojanahary.getId()), LINKED.toString());
    managerHasina = userRepository.save(hasina());
    adminUser = userRepository.save(adminMialy());
    userRepository.saveAll(List.of(teacherToky));
    courseRepository.saveAll(List.of(courseProg1, courseProg2));
    groupFlowRepository.saveAll(List.of(groupFlowAxel, groupFlowTolojanahary, groupFlowOtherGroup));
    courseAssignmentRepository.saveAll(List.of(assignProg1ToTokyForG1, assignProg2ToTokyForG2));
    examRepository.save(exam1Prog1);
    examRepository.save(exam2Prog1);
    exam1Prog1.setGrades(gradesExam1Prog1);
    exam2Prog1.setGrades(gradesExam2Prog1);
    gradeRepository.saveAll(gradesExam1Prog1);
    gradeRepository.saveAll(gradesExam2Prog1);

    groupIds.addAll(List.of(groupG1.getId(), groupG2.getId()));
    studentIds.addAll(
        List.of(
            studentAxel.getId(),
            studentTolojanahary.getId(),
            studentInOtherGroup.getId(),
            monitorOfAxel.getId(),
            monitorOfTolojanahary.getId(),
            managerHasina.getId(),
            adminUser.getId()));
    groupFlowIds.addAll(
        List.of(groupFlowAxel.getId(), groupFlowTolojanahary.getId(), groupFlowOtherGroup.getId()));
    teacherIds.add(teacherToky.getId());
    courseIds.addAll(List.of(courseProg1.getId(), courseProg2.getId()));
    courseAssignmentIds.addAll(
        List.of(assignProg1ToTokyForG1.getId(), assignProg2ToTokyForG2.getId()));
    examIds.addAll(List.of(exam1Prog1.getId(), exam2Prog1.getId()));
  }

  @BeforeEach
  void setUp() {
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    adminToken = tokenFor(casdoorAuthServiceMock, adminUser);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
    // a monitor authenticates as a student, per the casdoor role mapping
    axelMonitorToken =
        tokenFor(casdoorAuthServiceMock, monitorOfAxel.getEmail(), User.Role.STUDENT);
  }

  @AfterEach
  void tearDown() {
    gradeRepository.deleteAll(
        gradeRepository.findAll().stream()
            .filter(g -> g.getExam() != null && examIds.contains(g.getExam().getId()))
            .toList());
    examRepository.deleteAllById(examIds);
    courseAssignmentRepository.deleteAllById(courseAssignmentIds);
    groupFlowRepository.deleteAllById(groupFlowIds);
    courseRepository.deleteAllById(courseIds);
    groupRepository.deleteAllById(groupIds);
    // the monitor owns the join rows (joinColumns = monitor_id), so clearing its collection drops
    // them before the users they point at
    clearFollowedStudents(monitorOfAxel.getId());
    clearFollowedStudents(monitorOfTolojanahary.getId());
    userRepository.deleteAllById(studentIds);
    userRepository.deleteAllById(teacherIds);

    studentIds.clear();
    teacherIds.clear();
    groupIds.clear();
    courseIds.clear();
    courseAssignmentIds.clear();
    examIds.clear();
    groupFlowIds.clear();
  }

  @Test
  void student_read_other_grade_ko() {
    var api = new GradesApi(anApiClient(axelToken));

    assertThrowsForbiddenException(
        () -> api.getGradesByStudentId(studentTolojanahary.getId(), 1, 10));
    assertThrowsForbiddenException(
        () -> api.getParticipantGrade(groupG1.getId(), exam1Prog1.getId()));
  }

  @Test
  void manager_crupdate_invalid_grade_ko() {
    var api = new GradesApi(anApiClient(managerToken));
    var newGrade = new CreateGrade().score(28.2);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"score must be between 0 and 20\"}",
        () ->
            api.createParticipantGrade(exam1Prog1.getId(), studentInOtherGroup.getId(), newGrade));
  }

  @Test
  void manager_crupdate_grade_invalid_student_ko() {
    when(promotionService.getAllStudentPromotions(any()))
        .thenReturn(new LinkedHashSet<>(List.of(promotionH())));
    var api = new GradesApi(anApiClient(managerToken));
    var newGrade = new CreateGrade().score(18.2);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Student with id "
            + studentInOtherGroup.getId()
            + " is not in exam "
            + exam2Prog1.getId()
            + "\"}",
        () ->
            api.createParticipantGrade(exam2Prog1.getId(), studentInOtherGroup.getId(), newGrade));
  }

  @Test
  void student_crupdate_grade_forbidden() {
    var api = new GradesApi(anApiClient(axelToken));
    var updateGrade =
        new UpdateGrade()
            .comment("Rectification")
            .studentRef(studentAxel.getRef())
            .grade(new CreateGrade().score(90.0));

    assertThrowsForbiddenException(
        () -> api.correctParticipantGrade(exam1Prog1.getId(), studentAxel.getId(), updateGrade));
  }

  @Test
  void student_get_all_grade_ko() {
    var api = new GradesApi(anApiClient(axelToken));

    assertThrowsForbiddenException(
        () -> api.getStudentGradesForExam(exam1Prog1.getId(), 1, 10, null));
  }

  @Test
  void teacher_or_manager_or_admin_get_course_grades_ok() throws ApiException {
    var managerApi = new GradesApi(anApiClient(managerToken));
    var adminApi = new GradesApi(anApiClient(adminToken));
    var teacherApi = new GradesApi(anApiClient(teacherToken));

    var adminAxelGrades =
        adminApi.getCourseGrades(studentAxel.getId(), courseProg1.getId(), null, null, null);
    var managerAxelGrades =
        managerApi.getCourseGrades(studentAxel.getId(), courseProg1.getId(), null, null, null);
    var teacherAxelGrades =
        teacherApi.getCourseGrades(studentAxel.getId(), courseProg1.getId(), null, null, null);

    assertGradeExists(adminAxelGrades, axelGradeExam1Prog1);
    assertGradeExists(adminAxelGrades, axelGradeExam2Prog1);
    assertGradeExists(managerAxelGrades, axelGradeExam1Prog1);
    assertGradeExists(managerAxelGrades, axelGradeExam2Prog1);
    assertGradeExists(teacherAxelGrades, axelGradeExam1Prog1);
    assertGradeExists(teacherAxelGrades, axelGradeExam2Prog1);
  }

  @Test
  void manager_read_by_student_id_ok() throws ApiException {
    var api = new GradesApi(anApiClient(managerToken));

    var axelGrades = api.getGradesByStudentId(studentAxel.getId(), 1, 10);

    assertGradeExists(axelGrades, axelGradeExam1Prog1);
    assertGradeExists(axelGrades, axelGradeExam2Prog1);
  }

  @Test
  void student_get_course_grades_ko() {
    var api = new GradesApi(anApiClient(axelToken));

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getCourseGrades(studentAxel.getId(), courseProg1.getId(), null, null, null));
  }

  @Test
  void get_grades_for_student_with_unassigned_course_ko() {
    var monitorApi = new GradesApi(anApiClient(axelMonitorToken));

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Student's current group is not assigned to"
            + " course with id: %s\"}".formatted(courseProg2.getId()),
        () ->
            monitorApi.getCourseGrades(studentAxel.getId(), courseProg2.getId(), null, null, null));
  }

  @Test
  void monitor_get_grades_of_other_student_ko() {
    var monitorApi = new GradesApi(anApiClient(axelMonitorToken));

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () ->
            monitorApi.getCourseGrades(
                studentTolojanahary.getId(), courseProg1.getId(), null, null, null));
  }

  @Test
  void monitor_get_own_grades_ok() throws ApiException {
    var monitorApi = new GradesApi(anApiClient(axelMonitorToken));

    var axelGrades =
        monitorApi.getCourseGrades(studentAxel.getId(), courseProg1.getId(), null, null, null);

    assertGradeExists(axelGrades, axelGradeExam1Prog1);
    assertGradeExists(axelGrades, axelGradeExam2Prog1);
  }

  @Test
  void monitor_get_own_yearly_result_ok() {
    var monitorApi = new GradesApi(anApiClient(axelMonitorToken));

    assertDoesNotThrow(() -> monitorApi.getYearlyResult(studentAxel.getId(), L1));
  }

  @Test
  void monitor_get_own_result_summary_ok() {
    var monitorApi = new GradesApi(anApiClient(axelMonitorToken));

    assertDoesNotThrow(() -> monitorApi.getResultsSummary(studentAxel.getId()));
  }

  @Test
  void monitor_get_other_yearly_result_ko() {
    var monitorApi = new GradesApi(anApiClient(axelMonitorToken));

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> monitorApi.getYearlyResult(studentTolojanahary.getId(), L1));
  }

  @Test
  void monitor_get_else_yearly_result_transcript_ko() {
    var monitorApi = new GradesApi(anApiClient(axelMonitorToken));

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> monitorApi.getYearlyResultTranscript(studentTolojanahary.getId(), L1));
  }

  @Test
  void monitor_get_exam_own_grades_ok() throws ApiException {
    var monitorApi = new GradesApi(anApiClient(axelMonitorToken));

    var actual = monitorApi.getParticipantGrade(exam1Prog1.getId(), studentAxel.getId());
    var expected = gradeMapper.toRest(axelGradeExam1Prog1);

    clearTimestamps(actual);
    clearTimestamps(expected);
    assertEquals(expected, actual);
  }

  @Test
  void create_existing_grade_ko() {
    var gradesApi = new GradesApi(anApiClient(managerToken));
    var axelId = studentAxel.getId();
    var examId = exam1Prog1.getId();
    when(promotionService.getAllStudentPromotions(axelId))
        .thenReturn(new LinkedHashSet<>(List.of(promotionH())));
    var createGrades = List.of(new CreateGrade().score(10.).studentId(axelId));

    assertBadRequestException(
        "Grade for the student %s for the exam %s already exist".formatted(axelId, examId),
        () -> gradesApi.createParticipantsGradeForExam(examId, createGrades));
  }

  @Test
  void create_grade_ok() throws ApiException {
    when(promotionService.getAllStudentPromotions(any()))
        .thenReturn(new LinkedHashSet<>(List.of(promotionH())));

    // tolojanahary is in G1 and so in exam2, but has no grade there yet
    var examId = exam2Prog1.getId();
    var studentId = studentTolojanahary.getId();
    var createGrade = new CreateGrade().score(10.).studentId(studentId);

    var createdGrades =
        new GradesApi(anApiClient(managerToken))
            .createParticipantsGradeForExam(examId, List.of(createGrade));

    assertEquals(1, createdGrades.size());
    var createdGrade = createdGrades.getFirst();
    assertEquals(studentId, createdGrade.getStudent().getId());
    assertEquals(examId, createdGrade.getGrade().getExam().getId());
    assertEquals(createGrade.getScore(), createdGrade.getGrade().getScore());
  }

  @Test
  void monitor_get_grade_history_ok() throws ApiException {
    var gradesApi = new GradesApi(anApiClient(managerToken));
    var initialGrade = gradeRepository.save(axelGradeExam1Prog1);

    var firstScore = faker.number().randomDouble(0, 20, 2);
    var firstModification = initialGrade.toBuilder().build();
    firstModification.setScore(firstScore, faker.lorem().paragraph(2));
    firstModification = gradeRepository.save(firstModification);

    var secondScore = faker.number().randomDouble(0, 20, 2);
    var secondModification = firstModification.toBuilder().build();
    secondModification.setScore(secondScore, faker.lorem().paragraph(2));
    gradeRepository.save(secondModification);

    var gradeHistory =
        gradesApi.getOrderedGradeHistory(initialGrade.getId(), null, null, null, null, null);

    assertEquals(2, gradeHistory.size());
    assertEquals(firstScore, gradeHistory.getFirst().getScore());
    assertEquals(secondScore, gradeHistory.get(1).getScore());
  }

  private void clearFollowedStudents(String monitorId) {
    userRepository
        .findById(monitorId)
        .ifPresent(
            monitor -> {
              monitor.setMonitors(new ArrayList<>());
              userRepository.save(monitor);
            });
  }

  private static void clearTimestamps(school.hei.haapi.endpoint.rest.model.Grade grade) {
    grade
        .createdAt(null)
        .updateDate(null)
        .getExam()
        .getCourseAssignment()
        .getGroups()
        .forEach(group -> group.setCreationDatetime(null));
  }

  private void assertGradeExists(
      List<school.hei.haapi.endpoint.rest.model.Grade> grades, Grade expectedEntity) {
    var expected = gradeMapper.toRest(expectedEntity);
    assertTrue(
        grades.stream()
            .anyMatch(
                grade ->
                    expected.getId().equals(grade.getId())
                        && expected.getExam().getId().equals(grade.getExam().getId())
                        && grade.getScore().equals(expected.getScore())));
  }
}
