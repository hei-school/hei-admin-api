package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE_ASSIGNMENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.EXAM1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertBadRequestException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.course1;
import static school.hei.haapi.integration.conf.TestUtils.courseAssignment1;
import static school.hei.haapi.integration.conf.TestUtils.createExam;
import static school.hei.haapi.integration.conf.TestUtils.createExam1;
import static school.hei.haapi.integration.conf.TestUtils.exam1;
import static school.hei.haapi.integration.conf.TestUtils.exam2;
import static school.hei.haapi.integration.conf.TestUtils.exam3;
import static school.hei.haapi.integration.conf.TestUtils.exam4;
import static school.hei.haapi.integration.conf.TestUtils.exam5;
import static school.hei.haapi.integration.conf.TestUtils.grade1;
import static school.hei.haapi.integration.conf.TestUtils.grade2;
import static school.hei.haapi.integration.conf.TestUtils.group1;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.conf.TestUtils.studentGrade1;
import static school.hei.haapi.integration.conf.TestUtils.teacher1;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.ExamsApi;
import school.hei.haapi.endpoint.rest.api.GradesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Exam;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;

@Testcontainers
@AutoConfigureMockMvc
class ExamIT extends FacadeITMockedThirdParties {
  // TODO: some resources are not implemented yet then test failed
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
  void student_read_exam_grades_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    ExamsApi api = new ExamsApi(student1Client);
    assertThrowsForbiddenException(() -> api.getExamById(COURSE_ASSIGNMENT1_ID, EXAM1_ID));
  }

  @Test
  void manager_read_exam_details_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    GradesApi api = new GradesApi(manager1Client);

    List<StudentGrade> studentGrades = api.getStudentGradesForExam(EXAM1_ID, 1, 1);

    StudentGrade grade = studentGrades.getFirst();
    grade.getGrade().updateDate(null);
    assertEquals(studentGrade1(), grade);
  }

  @Test
  void student_create_or_update_exam_ko() {
    ExamsApi api = new ExamsApi(anApiClient(STUDENT1_TOKEN));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateExams(COURSE_ASSIGNMENT1_ID, List.of(exam1())));
  }

  @Test
  void teacher_create_exam_ok() throws ApiException {
    ExamsApi examsApi = new ExamsApi(anApiClient(TEACHER1_TOKEN));

    var crupdatedExam = createExam();
    var exams = examsApi.createOrUpdateExams(COURSE_ASSIGNMENT1_ID, List.of(crupdatedExam));
    assertEquals(1, exams.size());
    Exam exam = exams.getFirst();
    assertEquals(crupdatedExam.getCoefficient(), exam.getCoefficient());
    assertEquals(crupdatedExam.getExaminationDate(), exam.getExaminationDate());
    assertEquals(crupdatedExam.getTitle(), exam.getTitle());
  }

  // TODO : check test data because student_1 is now in group_2 according to group_flows4_id
  @Test
  void exam_creation_create_only_one_exam() throws ApiException {
    ExamsApi api = new ExamsApi(anApiClient(TEACHER1_TOKEN));
    int examCount = api.getAllExams(null, null, null, null, null, null, null, null, null).size();
    api.createOrUpdateExams(COURSE_ASSIGNMENT1_ID, List.of(createExam()));
    assertEquals(
        examCount + 1,
        api.getAllExams(null, null, null, null, null, null, null, null, null).size());
  }

  @Test
  void student_read_exam_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    ExamsApi api = new ExamsApi(student1Client);
    String exam1Id = exam1().getId();
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getExamOneExamById(exam1Id));
  }

  @Test
  void manager_read_exam_ko() {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    ExamsApi api = new ExamsApi(manager1Client);
    String nonExistentExamId = "NON_EXISTENT_EXAM";
    assertThrowsApiException(
        "{\"type\":\"404 NOT_FOUND\",\"message\":\"Exam with id #"
            + nonExistentExamId
            + " not found\"}",
        () -> api.getExamOneExamById(nonExistentExamId));
  }

  @Test
  void manager_read_exam_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    ExamsApi api = new ExamsApi(manager1Client);
    String exam1Id = exam1().getId();
    Exam actual = api.getExamOneExamById(exam1Id);
    assertDoesNotThrow(() -> api.getExamOneExamById(exam1Id));
    assertEquals(exam1(), actual);
  }

  @Test
  void teacher_read_exam_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    ExamsApi api = new ExamsApi(teacher1Client);
    String exam1Id = exam1().getId();
    Exam actual = api.getExamOneExamById(exam1Id);
    assertDoesNotThrow(() -> api.getExamOneExamById(exam1Id));
    assertEquals(actual, exam1());
  }

  @Test
  @Disabled("Don't pass in GHA")
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    ExamsApi api = new ExamsApi(manager1Client);
    List<Exam> actual =
        api.getAllExams(
            null, null, null, null, null, Instant.parse("2022-10-09T08:25:24Z"), null, 1, 10);

    assertTrue(actual.contains(exam1()));
    assertTrue(actual.contains(exam2()));
    assertTrue(actual.contains(exam3()));
    assertTrue(actual.contains(exam4()));
    assertTrue(actual.contains(exam5()));
  }

  @Test
  void filter_exam_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    ExamsApi api = new ExamsApi(manager1Client);
    List<Exam> filteredExams =
        api.getAllExams(
            courseAssignment1().getId(),
            teacher1().getId(),
            exam2().getTitle(),
            course1().getCode(),
            group1().getRef(),
            exam2().getExaminationDate().minusSeconds(1),
            null,
            1,
            10);

    assertEquals(1, filteredExams.size());
    assertEquals(exam2(), filteredExams.getFirst());
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    ExamsApi api = new ExamsApi(student1Client);
    assertThrowsForbiddenException(
        () -> api.getAllExams(null, null, null, null, null, null, null, 1, 10));
  }

  @Test
  @Disabled("Don't pass on GHA")
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    ExamsApi api = new ExamsApi(teacher1Client);
    List<Exam> actual = api.getAllExams(null, null, null, "", "", null, null, 1, 10);

    assertTrue(actual.contains(exam1()));
    assertTrue(actual.contains(exam2()));
    assertTrue(actual.contains(exam3()));
    assertTrue(actual.contains(exam4()));
    assertTrue(actual.contains(exam5()));
  }

  @Test
  void teacher_create_or_update_exam_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    ExamsApi api = new ExamsApi(teacher1Client);
    Exam actualCreate = api.createOrUpdateExamsInfos(createExam1());

    assertEquals("Algorithmics", actualCreate.getTitle());
    assertEquals(2, actualCreate.getCoefficient());
  }

  @Test
  void teacher_create_or_update_exam_with_bad_info_ko() {
    ExamsApi api = new ExamsApi(anApiClient(TEACHER1_TOKEN));

    assertBadRequestException(
        "Coefficient can't be less than 0",
        () -> api.createOrUpdateExamsInfos(createExam1().coefficient(-1)));
    assertBadRequestException(
        "Title is mandatory", () -> api.createOrUpdateExamsInfos(createExam1().title(null)));
    assertBadRequestException(
        "Examination date is mandatory",
        () -> api.createOrUpdateExamsInfos(createExam1().examinationDate(null)));
  }

  @Test
  void manager_create_or_update_exam_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    ExamsApi api = new ExamsApi(manager1Client);
    Exam actualCreate = api.createOrUpdateExamsInfos(createExam1());

    assertEquals("Algorithmics", actualCreate.getTitle());
    assertEquals(2, actualCreate.getCoefficient());
  }

  @Test
  void student_get_grade_for_each_exams_in_cours() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    GradesApi api = new GradesApi(student1Client);
    List<StudentExamGrade> studentExamsGrade = api.getStudentExamsGrade(COURSE1_ID, STUDENT1_ID);

    assertEquals(grade1().getScore(), studentExamsGrade.getFirst().getScore());
    assertEquals(exam1(), studentExamsGrade.getFirst().getExam());
    assertEquals(grade2().getScore(), studentExamsGrade.get(1).getScore());
    assertEquals(exam2(), studentExamsGrade.get(1).getExam());
  }

  @Test
  void admin_get_exam_by_id_ok() throws ApiException {
    ApiClient admin1Client = anApiClient(ADMIN1_TOKEN);
    ExamsApi api = new ExamsApi(admin1Client);
    Exam actual = api.getExamById(COURSE_ASSIGNMENT1_ID, EXAM1_ID);

    assertEquals(exam1(), actual);
  }
}
