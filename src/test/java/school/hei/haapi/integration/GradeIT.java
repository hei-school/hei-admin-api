package school.hei.haapi.integration;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.AwardedCourseExam;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = GradeIT.ContextInitializer.class)
@AutoConfigureMockMvc
class GradeIT {
  @MockBean
  private SentryConf sentryConf;

  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, GradeIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<AwardedCourseExam> actualAwardedCourseExamGrades = api.getStudentGrades(STUDENT1_ID, null);

    assertEquals(4, actualAwardedCourseExamGrades.size());
    assertTrue(actualAwardedCourseExamGrades.contains(awardedCourseExam1()));
    assertTrue(actualAwardedCourseExamGrades.contains(awardedCourseExam2()));
    assertTrue(actualAwardedCourseExamGrades.contains(awardedCourseExam3()));
    assertTrue(actualAwardedCourseExamGrades.contains(awardedCourseExam4()));

    ExamDetail actualExamDetail = api.getExamGrades(GROUP1_ID , EXAM1_ID, AWARDED_COURSE1_ID);
    assertEquals(examDetail1(), actualExamDetail);

    StudentGrade actuslStudentGrade = api.getParticipantGrade(GROUP1_ID , EXAM1_ID, STUDENT1_ID, AWARDED_COURSE1_ID);
    assertEquals(studentGrade1(), actuslStudentGrade);
  }
  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);

    List<AwardedCourseExam> actual = api.getStudentGrades(STUDENT1_ID, null);

    assertEquals(4, actual.size());
    assertTrue(actual.contains(awardedCourseExam1()));
    assertTrue(actual.contains(awardedCourseExam2()));
    assertTrue(actual.contains(awardedCourseExam3()));
    assertTrue(actual.contains(awardedCourseExam4()));


    ExamDetail actualExamDetail = api.getExamGrades(GROUP1_ID , EXAM1_ID, AWARDED_COURSE1_ID);
    assertEquals(examDetail1(), actualExamDetail);

    StudentGrade actuslStudentGrade = api.getParticipantGrade(GROUP1_ID , EXAM1_ID, STUDENT1_ID, AWARDED_COURSE1_ID);
    assertEquals(studentGrade1(), actuslStudentGrade);
  }

  @Test
  void student_read_his_grade_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);

    List<AwardedCourseExam> actual = api.getStudentGrades(STUDENT1_ID, null);

    assertEquals(4, actual.size());
    assertTrue(actual.contains(awardedCourseExam1()));
    assertTrue(actual.contains(awardedCourseExam2()));
    assertTrue(actual.contains(awardedCourseExam3()));
    assertTrue(actual.contains(awardedCourseExam4()));

    StudentGrade actuslStudentGrade = api.getParticipantGrade(GROUP1_ID , EXAM1_ID, STUDENT1_ID, AWARDED_COURSE1_ID);
    assertEquals(studentGrade1(), actuslStudentGrade);
  }

  @Test
  void student_read_other_grade_ko() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsForbiddenException(
            () -> api.getStudentGrades(STUDENT2_ID, null));
    assertThrowsForbiddenException(
            () -> api.getParticipantGrade(GROUP1_ID , EXAM1_ID, STUDENT3_ID, AWARDED_COURSE1_ID));
  }

  @Test
  void student_read_ko() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsForbiddenException(
        () -> api.getExamGrades(GROUP1_ID , EXAM1_ID, AWARDED_COURSE1_ID));
  }

  void manager_create_grades_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);
    List<ExamDetail> actual = api.createStudentExamGrade(GROUP1_ID, AWARDED_COURSE1_ID, EXAM1_ID, List.of(createGrade(STUDENT1_ID,EXAM1_ID,AWARDED_COURSE1_ID)));
    assertEquals(1, actual.size());
  }

  void teacher_create_his_exam_grades_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);
    List<ExamDetail> actual = api.createStudentExamGrade(GROUP1_ID, AWARDED_COURSE1_ID, EXAM1_ID, List.of(createGrade(STUDENT1_ID,EXAM1_ID,AWARDED_COURSE1_ID)));
    assertEquals(1, actual.size());
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }

}
