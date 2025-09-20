package school.hei.haapi.integration;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.INVALIDATED;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.course1;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.test_data.RetakeExamSessionTestData.session1;

import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.RetakeExamApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.CourseResultStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateRetakeExam;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.repository.RetakeExamRepository;
import school.hei.haapi.service.GradeResultService;

@Testcontainers
@AutoConfigureMockMvc
public class RetakeExamIT extends FacadeITMockedThirdParties {
  @MockBean RetakeExamRepository retakeExamRepository;
  @MockBean GradeResultService gradeResultService;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setup() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());

    when(gradeResultService.getLeveledYearlyResultByStudentId(eq(L1), anyString()))
        .thenReturn(
            new YearlyResult()
                .level(L1)
                .status(INVALIDATED)
                .totalCredits(TEN)
                .courseResults(
                    List.of(
                        new CourseResult()
                            .course(course1())
                            .status(CourseResultStatus.INCOMPLETE)
                            .weightedAverage(ONE))));
    when(retakeExamRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
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
    retakeExam.setSessionId(session1().getId());

    var retakeExamsCreated = api.createOrUpdateRetakeExam(session1().getId(), List.of(retakeExam));
    assertNotNull(retakeExamsCreated);
    assertEquals(1, retakeExamsCreated.size());
    var retakeExamCreated = retakeExamsCreated.getFirst();
    assertEquals(retakeExam.getSessionId(), retakeExamCreated.getSession().getId());
    assertEquals(retakeExam.getCourseId(), retakeExamCreated.getCourse().getId());
    assertEquals(retakeExam.getStudentId(), retakeExamCreated.getStudentIdentifier().getId());
  }

  @Test
  void admin_read_all_retake_exams_by_session_ok() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var retakeExams =
        api.getRetakeExamBySessionId(session1().getId(), null, null, null, null, null, null, null);

    assertNotNull(retakeExams);
    assertEquals(3, retakeExams.size());
    assertEquals(
        "session1", Objects.requireNonNull(retakeExams.getFirst().getSession()).getTitle());
  }
}
