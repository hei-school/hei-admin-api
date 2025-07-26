package school.hei.haapi.integration;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.EXAM1_ID;
import static school.hei.haapi.integration.conf.TestUtils.EXAM3_ID;
import static school.hei.haapi.integration.conf.TestUtils.GROUP1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT3_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.courseAssignmentExam1;
import static school.hei.haapi.integration.conf.TestUtils.courseAssignmentExam2;
import static school.hei.haapi.integration.conf.TestUtils.courseAssignmentExam4;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.conf.TestUtils.studentGrade1;
import static school.hei.haapi.integration.conf.TestUtils.studentGrade7;
import static school.hei.haapi.integration.test_data.CourseAssignmentTestData.createCourseAssignment;
import static school.hei.haapi.integration.test_data.CourseTestData.prog1;
import static school.hei.haapi.integration.test_data.CourseTestData.prog2;
import static school.hei.haapi.integration.test_data.CourseTestData.prog4;
import static school.hei.haapi.integration.test_data.ExamTestData.createExam;
import static school.hei.haapi.integration.test_data.GradeTestData.createRandomGrades;
import static school.hei.haapi.integration.test_data.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.test_data.GroupTestData.g1;
import static school.hei.haapi.integration.test_data.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.test_data.MonitorTestData.monitorOfTolojanahary;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.test_data.TeacherTestData.toky;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.GradesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.CourseAssignmentExam;
import school.hei.haapi.endpoint.rest.model.CrupdateGrade;
import school.hei.haapi.endpoint.rest.model.Grade;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
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
  @Autowired ExamRepository examRepository;
  @Autowired GradeMapper gradeMapper;
  private User studentAxel;
  private User studentTolojanahary;
  private User monitorOfAxel;
  private User monitorOfTolojanahary;
  private Course courseProg1;
  private User teacherToky;
  private Exam exam1Prog1;
  private CourseAssignment assign_web1_toToky;
  private Group groupG1;
  private List<school.hei.haapi.model.Grade> gradesExam1Prog1;
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
    studentAxel = axel();
    studentTolojanahary = tolojanahary();
    monitorOfAxel = monitorOfAxel();
    monitorOfTolojanahary = monitorOfTolojanahary();
    courseProg1 = prog1();
    teacherToky = toky();
    groupFlowsAxel = createGroupFlow(studentAxel, groupG1);
    groupFlowsTolojanahary = createGroupFlow(studentTolojanahary, groupG1);
    assign_web1_toToky = createCourseAssignment(courseProg1, teacherToky, List.of(groupG1));
    exam1Prog1 =
        createExam(Instant.parse("2025-07-22T10:15:30Z"), assign_web1_toToky, gradesExam1Prog1);
    gradesExam1Prog1 = createRandomGrades(List.of(studentAxel, studentTolojanahary), exam1Prog1);

    groupRepository.save(groupG1);
    userRepository.saveAll(List.of(studentAxel, studentTolojanahary));
    userRepository.saveAll(List.of(teacherToky));
    courseRepository.save(courseProg1);
    groupFlowRepository.saveAll(List.of(groupFlowsAxel, groupFlowsTolojanahary));
    courseAssignmentRepository.save(assign_web1_toToky);
    examRepository.save(exam1Prog1);
    gradeRepository.saveAll(gradesExam1Prog1);

    groupIds.add(groupG1.getId());
    studentIds.addAll(List.of(studentAxel.getId(), studentTolojanahary.getId()));
    groupFlowIds.addAll(List.of(groupFlowsAxel.getId(), groupFlowsTolojanahary.getId()));
    teacherIds.add(teacherToky.getId());
    courseIds.add(courseProg1.getId());
    courseAssignmentIds.add(assign_web1_toToky.getId());
    examIds.add(exam1Prog1.getId());
    gradeIds.addAll(List.of(gradesExam1Prog1.get(0).getId(), gradesExam1Prog1.get(1).getId()));
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
    setUpTestData();
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    GradesApi api = new GradesApi(manager1Client);

    List<CourseAssignmentExam> actualAwardedCourseExamGrades =
        api.getStudentGrades(STUDENT1_ID, 1, 10);

    assertTrue(actualAwardedCourseExamGrades.contains(courseAssignmentExam1()));
    assertTrue(actualAwardedCourseExamGrades.contains(courseAssignmentExam2()));
    assertTrue(actualAwardedCourseExamGrades.contains(courseAssignmentExam4()));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    GradesApi api = new GradesApi(teacher1Client);

    List<CourseAssignmentExam> actual = api.getStudentGrades(STUDENT1_ID, 1, 10);
    assertTrue(actual.contains(courseAssignmentExam1()));
    assertTrue(actual.contains(courseAssignmentExam2()));
    assertTrue(actual.contains(courseAssignmentExam4()));
  }

  @Test
  void student_read_his_grade_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    GradesApi api = new GradesApi(student1Client);

    List<CourseAssignmentExam> actual = api.getStudentGrades(STUDENT1_ID, 1, 10);

    assertTrue(actual.contains(courseAssignmentExam1()));
    assertTrue(actual.contains(courseAssignmentExam2()));
    assertTrue(actual.contains(courseAssignmentExam4()));
  }

  @Test
  void student_read_other_grade_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    GradesApi api = new GradesApi(student1Client);
    assertThrowsForbiddenException(() -> api.getStudentGrades(STUDENT2_ID, 1, 10));
    assertThrowsForbiddenException(() -> api.getParticipantGrade(GROUP1_ID, EXAM1_ID));
  }

  @Test
  void manager_crupdate_invalid_grade_ko() {
    ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
    GradesApi api = new GradesApi(managerClient);

    CrupdateGrade newGrade = new CrupdateGrade();
    newGrade.setScore(28.2);

    ApiException illegalArgumentException =
        assertThrows(
            ApiException.class,
            () -> api.crupdateParticipantGrade(EXAM1_ID, STUDENT3_ID, newGrade));

    String exceptedMessage = "score must be between 0 and 20";
    String actualMessage = illegalArgumentException.getMessage();

    assertTrue(actualMessage.contains(exceptedMessage));
  }

  @Test
  void manager_crupdate_grade_invalid_student_ko() {
    GradesApi api = new GradesApi(anApiClient(MANAGER1_TOKEN));
    CrupdateGrade newGrade = new CrupdateGrade();
    newGrade.setScore(18.2);

    ApiException illegalArgumentException =
        assertThrows(
            ApiException.class,
            () -> api.crupdateParticipantGrade(EXAM3_ID, STUDENT3_ID, newGrade));

    String exceptedMessage = "Student with id: " + STUDENT3_ID + " not in the Exam: " + EXAM3_ID;
    String actualMessage = illegalArgumentException.getMessage();

    assertTrue(actualMessage.contains(exceptedMessage));
  }

  @Test
  void student_crupdate_grade_forbidden() {
    ApiClient studentClient = anApiClient(STUDENT1_TOKEN);
    GradesApi api = new GradesApi(studentClient);

    CrupdateGrade newCrupdateGrade = new CrupdateGrade();
    newCrupdateGrade.setScore(90.0);

    assertThrowsForbiddenException(
        () -> api.crupdateParticipantGrade(EXAM1_ID, STUDENT1_ID, newCrupdateGrade));
  }

  @Test
  void teacher_get_all_grade_ok() throws ApiException {
    GradesApi managerApi = new GradesApi(anApiClient(MANAGER1_TOKEN));

    List<StudentGrade> participantsGradeForExam =
        managerApi.getParticipantsGradeForExam(EXAM1_ID, 1, 2);

    assertNotNull(participantsGradeForExam);
    assertTrue(participantsGradeForExam.containsAll(List.of(studentGrade1(), studentGrade7())));
  }

  @Test
  void student_get_all_grade_ko() {
    GradesApi studentApi = new GradesApi(anApiClient(STUDENT1_TOKEN));

    assertThrowsForbiddenException(() -> studentApi.getParticipantsGradeForExam(EXAM1_ID, 1, 10));
  }

  @Test
  void manager_or_admin_get_course_grades_ok() throws ApiException {
    GradesApi managerApi = new GradesApi(anApiClient(MANAGER1_TOKEN));
    GradesApi adminApi = new GradesApi(anApiClient(ADMIN1_TOKEN));
    List<Grade> adminAxelGrades =
        adminApi.getCourseGrades(axel().getId(), prog1().getId(), StudentLevel.L1);
    List<Grade> managerAxelGrades =
        managerApi.getCourseGrades(axel().getId(), prog1().getId(), StudentLevel.L1);

    List<Grade> allProg1Grades = gradesExam1Prog1.stream().map(gradeMapper::toRest).toList();

    assertTrue(allProg1Grades.containsAll(adminAxelGrades));
    assertTrue(allProg1Grades.containsAll(managerAxelGrades));
    // TODO: check that all the grades are his.
    adminAxelGrades.stream()
        .map(gradeMapper::toRestStudentGrade)
        .allMatch(studentGrade -> studentGrade.getStudent().getId().equals(axel().getId()));
  }

  @Test
  void manager_or_admin_get_course_grades_with_incomplete_exams_ok() throws ApiException {
    GradesApi managerApi = new GradesApi(anApiClient(MANAGER1_TOKEN));
    GradesApi adminApi = new GradesApi(anApiClient(ADMIN1_TOKEN));

    managerApi.getCourseGrades(axel().getId(), prog1().getId(), StudentLevel.L1);
    adminApi.getCourseGrades(axel().getId(), prog1().getId(), StudentLevel.L1);
  }

  @Test
  void student_get_course_grades_ko() throws ApiException {
    GradesApi studentApi = new GradesApi(anApiClient(STUDENT1_TOKEN));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> studentApi.getCourseGrades(axel().getId(), prog1().getId(), StudentLevel.L1));
  }

  @Test
  void get_grades_for_student_with_unassigned_course_ko() throws ApiException {
    GradesApi monitorApi = new GradesApi(anApiClient(MONITOR1_TOKEN));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST \",\"message\":\"Course %s is not assigned to student with id: %s\"}"
            .formatted(prog2().getId(), axel().getId()),
        () -> monitorApi.getCourseGrades(axel().getId(), prog4().getId(), StudentLevel.L1));
  }

  @Test
  void monitor_get_grades_of_other_student_ko() throws ApiException {
    GradesApi monitorApi = new GradesApi(anApiClient(monitorOfAxel.getId()));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> monitorApi.getCourseGrades(tolojanahary().getId(), prog1().getId(), StudentLevel.L1));
  }

  @Test
  void monitor_get_own_grades_ok() throws ApiException {
    GradesApi monitorApi = new GradesApi(anApiClient(monitorOfAxel.getId()));
    List<Grade> axelGrades =
        monitorApi.getCourseGrades(axel().getId(), prog1().getId(), StudentLevel.L1);
  }
}
