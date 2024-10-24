package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.*;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = ExamIT.ContextInitializer.class)
@AutoConfigureMockMvc
class ExamIT extends MockedThirdParties {
  // TODO: some resources are not implemented yet then test failed
  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ExamIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
  }

  /*
    @Test
    void manager_read_ok() throws ApiException {
      ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
      TeachingApi api = new TeachingApi(manager1Client);

      List<ExamInfo> actual = api.getExamsByAwardedCourse(AWARDED_COURSE1_ID, 1, 10);

      ExamInfo oneActualExam = api.getExamById(AWARDED_COURSE1_ID, EXAM1_ID);

      assertEquals(2, actual.size());
      assertTrue(actual.contains(exam1()));
      assertTrue(actual.contains(exam2()));

      assertEquals(exam1(), oneActualExam);
    }

    @Test
    void student_read_ko() {
      ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
      TeachingApi api = new TeachingApi(student1Client);
      assertThrowsForbiddenException(() -> api.getExamsByAwardedCourse(AWARDED_COURSE1_ID, 1, 10));
    }

    @Test
    void teacher_read_ok() throws ApiException {
      ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
      TeachingApi api = new TeachingApi(teacher1Client);
      List<ExamInfo> actual = api.getExamsByAwardedCourse(AWARDED_COURSE1_ID, 1, 10);
      ExamInfo oneActualExam = api.getExamById(AWARDED_COURSE1_ID, EXAM1_ID);

      assertEquals(2, actual.size());
      assertTrue(actual.contains(exam1()));
      assertTrue(actual.contains(exam2()));

      assertEquals(exam1(), oneActualExam);
    }

    //  @Test
    //  void student_read_exam_grades_ko() {
    //    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    //    TeachingApi api = new TeachingApi(student1Client);
    //    assertThrowsForbiddenException(
    //        () -> api.get(GROUP1_ID, EXAM1_ID, AWARDED_COURSE1_ID));
    //  }

    //  @Test
    //  void manager_read_exam_details_ok() throws ApiException {
    //    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    //    TeachingApi api = new TeachingApi(manager1Client);
    //    ExamDetail actual = api.getExamGrades(GROUP1_ID, EXAM1_ID, AWARDED_COURSE1_ID);
    //    assertEquals(examDetail1(), actual);
    //  }
    //
    //  void student_create_or_update_exam_ko() throws ApiException {
    //    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    //    TeachingApi api = new TeachingApi(student1Client);
    //    assertThrowsApiException(
    //        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
    //        () -> api.createOrUpdateExams(GROUP1_ID, AWARDED_COURSE1_ID, List.of(exam1())));
    //  }
  */
  @Test
  @DirtiesContext
  void teacher_create_or_update_exam_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);
    ExamInfo actualCreat = api.createOrUpdateExamsInfos(createExam1());

    assertEquals(exam1(), actualCreat);
  }

  @Test
  @DirtiesContext
  void teacher_create_or_update_exam_ko() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateExams(AWARDED_COURSE2_ID, List.of(exam2())));
  }

  @Test
  @DirtiesContext
  void manager_create_or_update_exam_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);
    ExamInfo actualCreat = api.createOrUpdateExamsInfos(createExam1());

    assertEquals(exam1(), actualCreat);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
