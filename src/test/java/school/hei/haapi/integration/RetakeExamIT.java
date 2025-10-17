package school.hei.haapi.integration;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.INVALIDATED;
import static school.hei.haapi.endpoint.rest.model.RetakeExamStatus.TO_CANCEL;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.course1;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.model.RetakeExamStatus.REGISTERED;

import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.RetakeExamApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.CourseResultStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateRetakeExam;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.repository.dao.RetakeExamDao;
import school.hei.haapi.service.GradeResultService;

@Testcontainers
@AutoConfigureMockMvc
public class RetakeExamIT extends FacadeITMockedThirdParties {
  @MockBean GradeResultService gradeResultService;
  @Autowired private RetakeExamDao retakeExamDao;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setup() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());

    when(gradeResultService.getStudentResultSummary(anyString()))
        .thenReturn(
            new ResultSummary()
                .yearlyResults(
                    List.of(
                        new YearlyResult()
                            .level(L1)
                            .status(INVALIDATED)
                            .totalCredits(TEN)
                            .courseResults(
                                List.of(
                                    new CourseResult()
                                        .course(course1())
                                        .status(CourseResultStatus.INCOMPLETE)
                                        .weightedAverage(ONE))))));
  }

  @Test
  void get_course_need_retake_by_student_ok() throws ApiException {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var retakeExams = api.getStudentRetakeExamBySession("student1_id", "session1_id");

    assertNotNull(retakeExams);
    assertEquals(
        Objects.requireNonNull(retakeExams.getFirst().getCourse()).getName(), course1().getName());
  }

  @Test
  void student_create_retake_exam_ok() throws ApiException {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);
    var retakeExam = new CrupdateRetakeExam();
    retakeExam.setStudentId(student1().getId());
    retakeExam.setCourseId(course1().getId());
    retakeExam.setSessionId("session2_id");
    retakeExam.setStatus(TO_CANCEL);

    var retakeExamsCreated = api.createOrUpdateRetakeExam("session2_id", List.of(retakeExam));

    assertNotNull(retakeExamsCreated);
    assertEquals(1, retakeExamsCreated.size());
    var retakeExamCreated = retakeExamsCreated.getFirst();
    assertEquals("session2_id", retakeExamCreated.getSession().getId());
    assertEquals(retakeExam.getCourseId(), retakeExamCreated.getCourse().getId());
    assertEquals(retakeExam.getStudentId(), retakeExamCreated.getStudentIdentifier().getId());
  }

  @Test
  void admin_read_all_retake_exams_by_session_ok() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var retakeExams =
        api.getRetakeExamBySessionId("session1_id", null, null, null, null, null, null);

    assertNotNull(retakeExams);
    assertEquals(
        "session1", Objects.requireNonNull(retakeExams.getFirst().getSession()).getTitle());
    assertNotNull(retakeExams.getFirst().getStudentIdentifier());
  }

  @Test
  void filter_retake_exam_by_status_ok() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var retakeExamFiltered =
        api.getRetakeExamBySessionId(
            "session2_id", null, List.of(TO_CANCEL), null, null, null, null);

    assertNotNull(retakeExamFiltered);
    assertEquals(2, retakeExamFiltered.size());
    assertEquals(TO_CANCEL, retakeExamFiltered.getFirst().getStatus());
  }

  @Test
  void get_all_retake_exam_courses_ok() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var courses = api.getRetakeExamCoursesBySessionId("session2_id", null, 1, 15);

    assertNotNull(courses);
    assertEquals(3, courses.size());
    assertEquals("IA2", courses.getFirst().getCode());
    assertEquals("PROG1", courses.get(1).getCode());
    assertEquals("PROG3", courses.get(2).getCode());
  }

  @Test
  void filter_retake_exam_course_by_course_code_ok() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var retakeExamCourse = api.getRetakeExamCoursesBySessionId("session2_id", "IA2", 1, 15);

    assertNotNull(retakeExamCourse);
    assertEquals(1, retakeExamCourse.size());
    assertEquals("Implemented IA", retakeExamCourse.getFirst().getName());
    assertEquals("IA2", retakeExamCourse.getFirst().getCode());
  }

  @Test
  void get_all_retake_exam_participants_of_course_ok() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var students =
        api.getRetakeExamParticipantByCourseIdAndSessionId(
            "session2_id", "course2_id", null, 1, 15);

    assertNotNull(students);
    assertEquals(1, students.size());
    assertEquals("student2_id", students.getFirst().getId());
  }

  @Test
  void filter_retake_exam_participants_by_student_ref_ok() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var participant =
        api.getRetakeExamParticipantByCourseIdAndSessionId(
            "session2_id", "course2_id", "STD21002", 1, 15);

    assertNotNull(participant);
    assertEquals("STD21002", participant.getFirst().getRef());
    assertEquals("student2_id", participant.getFirst().getId());
    assertEquals("Two", participant.getFirst().getFirstName());
    assertEquals("Student", participant.getFirst().getLastName());
  }

  @Test
  void filter_retake_exam_by_criteria_ok() {
    var pageable = PageRequest.of(0, 10);
    var retakeExams =
        retakeExamDao.filterByCriteria(null, null, null, null, null, List.of(REGISTERED), pageable);
    assertNotNull(retakeExams);
    assertEquals(1, retakeExams.size());
    assertEquals("retake_exam3_id", retakeExams.getFirst().getId());
    assertEquals(REGISTERED, retakeExams.getFirst().getStatus());
    assertEquals("student3_id", retakeExams.getFirst().getStudent().getId());
    assertEquals("course3_id", retakeExams.getFirst().getCourse().getId());
  }
}
