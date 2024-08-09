package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.*;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;

class ExamIT extends FacadeITMockedThirdParties {

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<ExamInfo> actual =
        api.getExamsByGroupIdAndAwardedCourse(GROUP1_ID, AWARDED_COURSE1_ID, 1, 10);

    ExamInfo oneActualExam = api.getExamById(GROUP1_ID, AWARDED_COURSE1_ID, EXAM1_ID);

    assertEquals(2, actual.size());
    assertTrue(actual.contains(exam1()));
    assertTrue(actual.contains(exam2()));

    assertEquals(exam1(), oneActualExam);
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
    ExamInfo oneActualExam = api.getExamById(GROUP1_ID, AWARDED_COURSE1_ID, EXAM1_ID);

    assertEquals(2, actual.size());
    assertTrue(actual.contains(exam1()));
    assertTrue(actual.contains(exam2()));

    assertEquals(exam1(), oneActualExam);
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
  @Disabled("was not annotated @Test")
  void student_create_or_update_exam_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateExams(GROUP1_ID, AWARDED_COURSE1_ID, List.of(exam1())));
  }

  @Test
  @DirtiesContext
  @Disabled("dirty")
  void teacher_create_or_update_his_awarded_course_exam_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);
    int numberOfExamToAdd = 3;
    List<ExamInfo> actualCreatList =
        api.createOrUpdateExams(
            GROUP1_ID, AWARDED_COURSE1_ID, someCreatableExamInfoList(numberOfExamToAdd));
    assertEquals(numberOfExamToAdd, actualCreatList.size());

    List<ExamInfo> actualUpdateList =
        api.createOrUpdateExams(GROUP1_ID, AWARDED_COURSE1_ID, List.of(exam1()));
    assertEquals(1, actualUpdateList.size());
    assertTrue(actualUpdateList.contains(exam1()));
  }

  @Disabled("was not annotated @Test")
  @Test
  void teacher_create_or_update_others_awarded_course_exam_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateExams(GROUP1_ID, AWARDED_COURSE2_ID, List.of(exam2())));
  }

  @Test
  @DirtiesContext
  @Disabled("dirty")
  void manager_create_or_update_exam_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);
    int numberOfExamToAdd = 3;
    List<ExamInfo> actualCreatList =
        api.createOrUpdateExams(
            GROUP1_ID, AWARDED_COURSE1_ID, someCreatableExamInfoList(numberOfExamToAdd));
    assertEquals(numberOfExamToAdd, actualCreatList.size());

    List<ExamInfo> actualUpdateList =
        api.createOrUpdateExams(GROUP1_ID, AWARDED_COURSE1_ID, List.of(exam1()));
    assertEquals(1, actualUpdateList.size());
    assertTrue(actualUpdateList.contains(exam1()));
  }
}
