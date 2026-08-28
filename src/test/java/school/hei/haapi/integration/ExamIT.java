package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.ApiAssertions.assertBadRequestException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsApiException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.CourseAssignmentTestData.createCourseAssignment;
import static school.hei.haapi.integration.testData.CourseTestData.prog1;
import static school.hei.haapi.integration.testData.ExamTestData.createExam;
import static school.hei.haapi.integration.testData.GradeTestData.createGrade;
import static school.hei.haapi.integration.testData.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.testData.GroupTestData.g1;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.StaffTestData.adminMialy;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.api.ExamsApi;
import school.hei.haapi.endpoint.rest.api.GradesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CrupdateExam;
import school.hei.haapi.endpoint.rest.model.Exam;
import school.hei.haapi.endpoint.rest.model.Fraction;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
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
import school.hei.haapi.repository.UserRepository;

class ExamIT extends FacadeITMockedThirdParties {
  private static final Instant FIRST_EXAM_DATE = Instant.parse("2022-10-09T08:25:24Z");
  private static final Instant SECOND_EXAM_DATE = Instant.parse("2022-11-09T08:25:24Z");

  @Autowired private UserRepository userRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  @Autowired private CourseAssignmentRepository courseAssignmentRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private GradeRepository gradeRepository;

  private User studentAxel;
  private User teacherToky;
  private User managerHasina;
  private User adminUser;
  private Course courseProg1;
  private Group groupG1;
  private CourseAssignment assignment;
  private school.hei.haapi.model.Exam firstExam;
  private school.hei.haapi.model.Exam secondExam;
  private Grade firstGrade;
  private Grade secondGrade;
  private GroupFlow axelJoinsG1;

  private String axelToken;
  private String teacherToken;
  private String managerToken;
  private String adminToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    teacherToky = userRepository.save(toky());
    managerHasina = userRepository.save(hasina());
    adminUser = userRepository.save(adminMialy());

    courseProg1 = courseRepository.save(prog1());
    groupG1 = groupRepository.save(g1());
    axelJoinsG1 = groupFlowRepository.save(createGroupFlow(studentAxel, groupG1));

    assignment =
        courseAssignmentRepository.save(
            createCourseAssignment(courseProg1, teacherToky, List.of(groupG1)));

    firstExam = examRepository.save(createExam(FIRST_EXAM_DATE, assignment));

    var databasesExam = createExam(SECOND_EXAM_DATE, assignment);
    databasesExam.setTitle("Databases");
    secondExam = examRepository.save(databasesExam);

    firstGrade = gradeRepository.save(createGrade(studentAxel, firstExam, 15));
    secondGrade = gradeRepository.save(createGrade(studentAxel, secondExam, 12));
  }

  @BeforeEach
  void setUp() {
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    adminToken = tokenFor(casdoorAuthServiceMock, adminUser);
  }

  @AfterEach
  void tearDown() {
    gradeRepository.deleteAll(List.of(firstGrade, secondGrade));
    examRepository.deleteAll(
        examRepository.findAll().stream()
            .filter(e -> e.getCourseAssignment() != null)
            .filter(e -> assignment.getId().equals(e.getCourseAssignment().getId()))
            .toList());
    courseAssignmentRepository.deleteById(assignment.getId());
    groupFlowRepository.deleteById(axelJoinsG1.getId());
    groupRepository.deleteById(groupG1.getId());
    courseRepository.deleteById(courseProg1.getId());
    userRepository.deleteAll(List.of(studentAxel, teacherToky, managerHasina, adminUser));
  }

  private ExamsApi examsApiAs(String token) {
    return new ExamsApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private CrupdateExam aCrupdateExam() {
    return new CrupdateExam()
        .coefficient(aFraction(2, 3))
        .title("Algorithmics")
        .courseAssignmentId(assignment.getId())
        .examinationDate(FIRST_EXAM_DATE);
  }

  @Test
  void student_read_exam_grades_ko() {
    var api = examsApiAs(axelToken);

    assertThrowsForbiddenException(() -> api.getExamById(assignment.getId(), firstExam.getId()));
  }

  @Test
  void manager_read_exam_details_ok() throws ApiException {
    var api = new GradesApi(anApiClient(managerToken));

    var studentGrades = api.getStudentGradesForExam(firstExam.getId(), 1, 1, null);

    var grade = studentGrades.getFirst();
    assertEquals(studentAxel.getId(), grade.getStudent().getId());
    assertEquals(firstGrade.getScore(), grade.getGrade().getScore());
  }

  @Test
  void student_create_or_update_exam_ko() {
    var api = examsApiAs(axelToken);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateExams(assignment.getId(), List.of(new Exam())));
  }

  @Test
  void teacher_create_exam_ok() throws ApiException {
    var api = examsApiAs(teacherToken);
    var toCreate =
        new Exam()
            .id(randomUUID().toString())
            .title("Algorithmics")
            .coefficient(aFraction(2, 3))
            .examinationDate(FIRST_EXAM_DATE);

    var exams = api.createOrUpdateExams(assignment.getId(), List.of(toCreate));

    assertEquals(1, exams.size());
    var exam = exams.getFirst();
    assertEquals(toCreate.getCoefficient(), exam.getCoefficient());
    assertEquals(toCreate.getExaminationDate(), exam.getExaminationDate());
    assertEquals(toCreate.getTitle(), exam.getTitle());
  }

  @Test
  void exam_creation_create_only_one_exam() throws ApiException {
    var api = examsApiAs(teacherToken);
    int examCount = api.getAllExams(null, null, null, null, null, null, null, null).size();

    api.createOrUpdateExams(
        assignment.getId(),
        List.of(
            new Exam()
                .id(randomUUID().toString())
                .title("Algorithmics")
                .coefficient(aFraction(2, 3))
                .examinationDate(FIRST_EXAM_DATE)));

    assertEquals(
        examCount + 1, api.getAllExams(null, null, null, null, null, null, null, null).size());
  }

  @Test
  void student_read_exam_ko() {
    var api = examsApiAs(axelToken);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getExamOneExamById(firstExam.getId()));
  }

  @Test
  void manager_read_exam_ko() {
    var api = examsApiAs(managerToken);
    var nonExistentExamId = "NON_EXISTENT_EXAM";

    assertThrowsApiException(
        "{\"type\":\"404 NOT_FOUND\",\"message\":\"Exam with id #"
            + nonExistentExamId
            + " not found\"}",
        () -> api.getExamOneExamById(nonExistentExamId));
  }

  @Test
  void manager_read_exam_ok() throws ApiException {
    var actual = examsApiAs(managerToken).getExamOneExamById(firstExam.getId());

    assertEquals(firstExam.getId(), actual.getId());
    assertEquals(firstExam.getTitle(), actual.getTitle());
    assertEquals(FIRST_EXAM_DATE, actual.getExaminationDate());
  }

  @Test
  void teacher_read_exam_ok() throws ApiException {
    var actual = examsApiAs(teacherToken).getExamOneExamById(firstExam.getId());

    assertEquals(firstExam.getId(), actual.getId());
    assertEquals(firstExam.getTitle(), actual.getTitle());
  }

  @Test
  void manager_read_ok() throws ApiException {
    var actual =
        examsApiAs(managerToken)
            .getAllExams(null, null, null, null, FIRST_EXAM_DATE.minusSeconds(1), null, 1, 100);

    assertTrue(idsOf(actual).contains(firstExam.getId()));
    assertTrue(idsOf(actual).contains(secondExam.getId()));
  }

  @Test
  void filter_exam_ok() throws ApiException {
    var filteredExams =
        examsApiAs(managerToken)
            .getAllExams(
                teacherToky.getId(),
                secondExam.getTitle(),
                courseProg1.getCode(),
                List.of(groupG1.getRef()),
                SECOND_EXAM_DATE.minusSeconds(1),
                null,
                1,
                10);

    assertEquals(1, filteredExams.size());
    assertEquals(secondExam.getId(), filteredExams.getFirst().getId());
  }

  @Test
  void student_read_ko() {
    var api = examsApiAs(axelToken);

    assertThrowsForbiddenException(
        () -> api.getAllExams(null, null, null, null, null, null, 1, 10));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    var actual = examsApiAs(teacherToken).getAllExams(null, null, "", null, null, null, 1, 100);

    assertTrue(idsOf(actual).contains(firstExam.getId()));
    assertTrue(idsOf(actual).contains(secondExam.getId()));
  }

  @Test
  void teacher_create_or_update_exam_ok() throws ApiException {
    var actualCreate = examsApiAs(teacherToken).createOrUpdateExamsInfos(aCrupdateExam());

    assertEquals("Algorithmics", actualCreate.getTitle());
    assertEquals(aFraction(2, 3), actualCreate.getCoefficient());
  }

  @Test
  void teacher_create_or_update_exam_with_bad_info_ko() {
    var api = examsApiAs(teacherToken);

    assertBadRequestException(
        "Components of the fraction cannot be less or equal than 0",
        () -> api.createOrUpdateExamsInfos(aCrupdateExam().coefficient(aFraction(-2, 0))));

    assertBadRequestException(
        "Components of the fraction cannot be null",
        () ->
            api.createOrUpdateExamsInfos(
                aCrupdateExam().coefficient(new Fraction().denominator(-2))));

    assertBadRequestException(
        "Title is mandatory", () -> api.createOrUpdateExamsInfos(aCrupdateExam().title(null)));

    assertBadRequestException(
        "Examination date is mandatory",
        () -> api.createOrUpdateExamsInfos(aCrupdateExam().examinationDate(null)));
  }

  @Test
  void manager_create_or_update_exam_ok() throws ApiException {
    var actualCreate = examsApiAs(managerToken).createOrUpdateExamsInfos(aCrupdateExam());

    assertEquals("Algorithmics", actualCreate.getTitle());
    assertEquals(aFraction(2, 3), actualCreate.getCoefficient());
  }

  @Test
  void student_get_grade_for_each_exams_in_cours() throws ApiException {
    var api = new GradesApi(anApiClient(axelToken));

    var studentExamsGrade = api.getStudentExamsGrade(courseProg1.getId(), studentAxel.getId());

    assertEquals(firstGrade.getScore(), studentExamsGrade.getFirst().getScore());
    assertEquals(firstExam.getId(), studentExamsGrade.getFirst().getExam().getId());
    assertEquals(secondGrade.getScore(), studentExamsGrade.get(1).getScore());
    assertEquals(secondExam.getId(), studentExamsGrade.get(1).getExam().getId());
  }

  @Test
  void admin_get_exam_by_id_ok() throws ApiException {
    var actual = examsApiAs(adminToken).getExamById(assignment.getId(), firstExam.getId());

    assertEquals(firstExam.getId(), actual.getId());
    assertEquals(firstExam.getTitle(), actual.getTitle());
  }

  private static List<String> idsOf(List<Exam> exams) {
    return exams.stream().map(Exam::getId).toList();
  }

  private static Fraction aFraction(int numerator, int denominator) {
    return new Fraction().numerator(numerator).denominator(denominator);
  }
}
