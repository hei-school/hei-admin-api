package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.TestUtils.AWARDED_COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.AWARDED_COURSE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.EXAM1_ID;
import static school.hei.haapi.integration.conf.TestUtils.GROUP1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.examDetail1;
import static school.hei.haapi.integration.conf.TestUtils.examInfo1;
import static school.hei.haapi.integration.conf.TestUtils.examInfo2;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.someCreatableExamInfoList;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.conf.FacadeIT;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.TestUtils;

@Testcontainers
class ExamIT extends FacadeIT {
  @LocalServerPort private int serverPort;
  @MockBean private SentryConf sentryConf;

  @MockBean private CognitoComponent cognitoComponentMock;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, serverPort);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<ExamInfo> actual =
        api.getExamsByGroupIdAndAwardedCourse(GROUP1_ID, AWARDED_COURSE1_ID, 1, 10);

    ExamInfo actualExam1 = api.getExamById(GROUP1_ID, AWARDED_COURSE1_ID, EXAM1_ID);

    assertTrue(actual.contains(examInfo1()));
    assertTrue(actual.contains(examInfo2()));
    assertEquals(examInfo1(), actualExam1);
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsForbiddenException(
        () -> api.getExamsByGroupIdAndAwardedCourse(GROUP1_ID, AWARDED_COURSE1_ID, 1, 10));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);
    List<ExamInfo> actual =
        api.getExamsByGroupIdAndAwardedCourse(GROUP1_ID, AWARDED_COURSE1_ID, 1, 10);
    ExamInfo actualExamInfo1 = api.getExamById(GROUP1_ID, AWARDED_COURSE1_ID, EXAM1_ID);

    assertTrue(actual.contains(examInfo1()));
    assertTrue(actual.contains(examInfo2()));
    assertEquals(examInfo1(), actualExamInfo1);
  }

  @Test
  void student_read_exam_grades_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsForbiddenException(
        () -> api.getExamGrades(GROUP1_ID, EXAM1_ID, AWARDED_COURSE1_ID));
  }

  @Test
  void manager_read_exam_details_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);
    ExamDetail actual = api.getExamGrades(GROUP1_ID, EXAM1_ID, AWARDED_COURSE1_ID);
    assertEquals(examDetail1(), actual);
  }

  @Test
  void student_create_or_update_exam_ko() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateExams(GROUP1_ID, AWARDED_COURSE1_ID, List.of(examInfo1())));
  }

  @Test
  void teacher_create_or_update_his_awarded_course_exam_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);
    int numberOfExamToAdd = 3;
    List<ExamInfo> creatableExamInfo =
        someCreatableExamInfoList(numberOfExamToAdd, AWARDED_COURSE1_ID);

    List<ExamInfo> actual =
        api.createOrUpdateExams(GROUP1_ID, AWARDED_COURSE1_ID, creatableExamInfo);
    ExamInfo createdExamInfo0 = actual.get(0);
    ExamInfo updatedExamInfo0 = update(createdExamInfo0);
    List<ExamInfo> updated =
        api.createOrUpdateExams(GROUP1_ID, AWARDED_COURSE1_ID, List.of(updatedExamInfo0));

    assertEquals(numberOfExamToAdd, actual.size());
    assertEquals(1, updated.size());
    assertTrue(updated.contains(updatedExamInfo0));
  }

  @Test
  void teacher_create_or_update_others_awarded_course_exam_ko() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateExams(GROUP1_ID, AWARDED_COURSE2_ID, List.of(examInfo2())));
  }

  @Test
  void manager_create_or_update_exam_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);
    int numberOfExamToAdd = 3;
    List<ExamInfo> creatableExamInfoList =
        someCreatableExamInfoList(numberOfExamToAdd, AWARDED_COURSE1_ID);

    List<ExamInfo> actual =
        api.createOrUpdateExams(GROUP1_ID, AWARDED_COURSE1_ID, creatableExamInfoList);
    ExamInfo createdExamInfo0 = actual.get(0);
    ExamInfo updatedExamInfo0 = update(createdExamInfo0);
    List<ExamInfo> updated =
        api.createOrUpdateExams(GROUP1_ID, AWARDED_COURSE1_ID, List.of(updatedExamInfo0));

    assertEquals(numberOfExamToAdd, actual.size());
    assertEquals(1, updated.size());
    assertTrue(updated.contains(updatedExamInfo0));
  }

  private ExamInfo update(ExamInfo examInfo) {
    Integer coefficient = examInfo.getCoefficient();
    int COEFFICIENT_ADD_VALUE = 10;
    return examInfo.coefficient(
        coefficient != null ? coefficient + COEFFICIENT_ADD_VALUE : COEFFICIENT_ADD_VALUE);
  }
}
