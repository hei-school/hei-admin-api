package school.hei.haapi.integration;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.EXAM1_ID;
import static school.hei.haapi.integration.conf.TestUtils.GROUP1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT3_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.awardedCourseExam1;
import static school.hei.haapi.integration.conf.TestUtils.awardedCourseExam2;
import static school.hei.haapi.integration.conf.TestUtils.awardedCourseExam4;
import static school.hei.haapi.integration.conf.TestUtils.exam1;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.conf.TestUtils.studentGrade1;
import static school.hei.haapi.integration.conf.TestUtils.studentGrade7;
import static school.hei.haapi.model.User.Status.ENABLED;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.AwardedCourseExam;
import school.hei.haapi.endpoint.rest.model.CrupdateGrade;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.endpoint.rest.model.UpdateGrade;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.UserService;

@Slf4j
@Testcontainers
@AutoConfigureMockMvc
class GradeIT extends FacadeITMockedThirdParties {
  @Autowired UserRepository userRepository;
  @Autowired UserService userService;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());

    User student = userService.findByRef(student1().getRef());
    student.setStatus(ENABLED);
    userRepository.save(student);
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<String> actualAwardedCourseExamGradesId =
        api.getStudentGrades(STUDENT1_ID, 1, 10).stream().map(AwardedCourseExam::getId).toList();

    assertTrue(actualAwardedCourseExamGradesId.contains(awardedCourseExam1().getId()));
    assertTrue(actualAwardedCourseExamGradesId.contains(awardedCourseExam2().getId()));
    assertTrue(actualAwardedCourseExamGradesId.contains(awardedCourseExam4().getId()));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);

    List<AwardedCourseExam> actual = api.getStudentGrades(STUDENT1_ID, 1, 10);

    assertTrue(actual.contains(awardedCourseExam1()));
    assertTrue(actual.contains(awardedCourseExam2()));
    assertTrue(actual.contains(awardedCourseExam4()));
  }

  @Test
  void student_read_his_grade_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);

    List<String> actualIds =
        api.getStudentGrades(STUDENT1_ID, 1, 10).stream().map(AwardedCourseExam::getId).toList();

    assertTrue(actualIds.contains(awardedCourseExam1().getId()));
    assertTrue(actualIds.contains(awardedCourseExam2().getId()));
    assertTrue(actualIds.contains(awardedCourseExam4().getId()));
  }

  @Test
  void student_read_other_grade_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsForbiddenException(() -> api.getStudentGrades(STUDENT2_ID, 1, 10));
    assertThrowsForbiddenException(() -> api.getParticipantGrade(GROUP1_ID, EXAM1_ID));
  }

  @Test
  @Disabled("Todo: move as dirty")
  void manager_crupdate_multiple_grade_ok() throws ApiException {
    ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(managerClient);

    UpdateGrade updateGrade =
        new UpdateGrade().grade(new CrupdateGrade().score(18.2)).studentRef(student1().getRef());
    List<StudentGrade> studentGrades =
        api.updateParticipantsGradeForExam(EXAM1_ID, List.of(updateGrade));

    assertEquals(1, studentGrades.size());
    assertEquals(updateGrade.getStudentRef(), studentGrades.getFirst().getStudent().getRef());
    assertEquals(
        updateGrade.getGrade().getScore() * exam1().getCoefficient(),
        studentGrades.getFirst().getGrade().getScore());
  }

  @Test
  void manager_crupdate_grade_ko() {
    ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(managerClient);

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
  void student_crupdate_grade_forbidden() {
    ApiClient studentClient = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(studentClient);

    CrupdateGrade newCrupdateGrade = new CrupdateGrade();
    newCrupdateGrade.setScore(90.0);

    assertThrowsForbiddenException(
        () -> api.crupdateParticipantGrade(EXAM1_ID, STUDENT1_ID, newCrupdateGrade));
  }

  @Test
  void teacher_get_all_grade_ok() throws ApiException {
    TeachingApi managerApi = new TeachingApi(anApiClient(MANAGER1_TOKEN));

    List<StudentGrade> participantsGradeForExam =
        managerApi.getParticipantsGradeForExam(EXAM1_ID, 1, 2);

    assertNotNull(participantsGradeForExam);
    assertTrue(participantsGradeForExam.containsAll(List.of(studentGrade1(), studentGrade7())));
  }

  @Test
  void student_get_all_grade_ko() {
    TeachingApi studentApi = new TeachingApi(anApiClient(STUDENT1_TOKEN));

    assertThrowsForbiddenException(() -> studentApi.getParticipantsGradeForExam(EXAM1_ID, 1, 10));
  }
}
