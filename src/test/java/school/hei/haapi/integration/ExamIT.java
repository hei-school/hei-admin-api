package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.AWARDED_COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.EXAM1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
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

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
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
    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsForbiddenException(() -> api.getExamById(AWARDED_COURSE1_ID, EXAM1_ID));
  }

  @Test
  void manager_read_exam_details_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);
    List<StudentGrade> studentGrades = api.getParticipantsGradeForExam(EXAM1_ID, 1, 1);
    assertEquals(studentGrade1(), studentGrades.getFirst());
  }

  @Test
  void student_create_or_update_exam_ko() {
    TeachingApi api = new TeachingApi(anApiClient(STUDENT1_TOKEN));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateExams(AWARDED_COURSE1_ID, List.of(exam1())));
  }

  @Test
  void teacher_create_exam_and_initialize_grades_ok() throws ApiException {
    TeachingApi api = new TeachingApi(anApiClient(TEACHER1_TOKEN));

    List<ExamInfo> exams = api.createOrUpdateExams(AWARDED_COURSE1_ID, List.of(createExam()));
    assertEquals(1, exams.size());
    ExamInfo exam = exams.getFirst();

    List<StudentGrade> studentGrades = api.getParticipantsGradeForExam(exam.getId(), 1, 10);
    assertEquals(
        api.getStudentsByGroupId(group1().getId(), 1, 10, null).size(), studentGrades.size());
    assertTrue(studentGrades.stream().allMatch(grade -> grade.getGrade().getScore() == 0));
  }

  @Test
  void exam_creation_create_only_one_exam() throws ApiException {
    TeachingApi api = new TeachingApi(anApiClient(TEACHER1_TOKEN));
    int examCount = api.getAllExams(null, null, null, null, null, null, null, null).size();
    api.createOrUpdateExams(AWARDED_COURSE1_ID, List.of(createExam()));
    assertEquals(
        examCount + 1, api.getAllExams(null, null, null, null, null, null, null, null).size());
  }

  @Test
  void student_read_exam_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);
    String exam1Id = exam1().getId();
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getExamOneExamById(exam1Id));
  }

  @Test
  void manager_read_exam_ko() {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);
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
    TeachingApi api = new TeachingApi(manager1Client);
    String exam1Id = exam1().getId();
    ExamInfo actual = api.getExamOneExamById(exam1Id);
    assertDoesNotThrow(() -> api.getExamOneExamById(exam1Id));
    assertEquals(actual, exam1());
  }

  @Test
  void teacher_read_exam_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);
    String exam1Id = exam1().getId();
    ExamInfo actual = api.getExamOneExamById(exam1Id);
    assertDoesNotThrow(() -> api.getExamOneExamById(exam1Id));
    assertEquals(actual, exam1());
  }

  @Test
  @Disabled("Don't pass in GHA")
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);
    List<ExamInfo> actual =
        api.getAllExams(null, null, null, null, Instant.parse("2022-10-09T08:25:24Z"), null, 1, 10);

    assertTrue(actual.contains(exam1()));
    assertTrue(actual.contains(exam2()));
    assertTrue(actual.contains(exam3()));
    assertTrue(actual.contains(exam4()));
    assertTrue(actual.contains(exam5()));
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsForbiddenException(
        () -> api.getAllExams(null, null, null, null, null, null, 1, 10));
  }

  @Test
  @Disabled("Don't pass on GHA")
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);
    List<ExamInfo> actual = api.getAllExams(null, null, "", "", null, null, 1, 10);

    assertTrue(actual.contains(exam1()));
    assertTrue(actual.contains(exam2()));
    assertTrue(actual.contains(exam3()));
    assertTrue(actual.contains(exam4()));
    assertTrue(actual.contains(exam5()));
  }

  @Test
  void teacher_create_or_update_exam_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);
    ExamInfo actualCreate = api.createOrUpdateExamsInfos(createExam1());

    assertEquals("Algorithmics", actualCreate.getTitle());
    assertEquals(2, actualCreate.getCoefficient());
  }

  @Test
  void manager_create_or_update_exam_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);
    ExamInfo actualCreate = api.createOrUpdateExamsInfos(createExam1());

    assertEquals("Algorithmics", actualCreate.getTitle());
    assertEquals(2, actualCreate.getCoefficient());
  }

  @Test
  void student_get_grade_for_each_exams_in_cours() throws ApiException {
    TeachingApi api = new TeachingApi(anApiClient(STUDENT1_TOKEN));
    List<StudentExamGrade> studentExamsGrade = api.getStudentExamsGrade(COURSE1_ID, STUDENT1_ID);

    assertEquals(grade1().getScore(), studentExamsGrade.getFirst().getScore());
    assertEquals(exam1(), studentExamsGrade.getFirst().getExam());
    assertEquals(grade2().getScore(), studentExamsGrade.get(1).getScore());
    assertEquals(exam2(), studentExamsGrade.get(1).getExam());
  }
}
