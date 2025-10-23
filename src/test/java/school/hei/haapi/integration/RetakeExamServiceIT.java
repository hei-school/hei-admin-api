package school.hei.haapi.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.INCOMPLETE;
import static school.hei.haapi.integration.conf.TestUtils.course1;
import static school.hei.haapi.integration.conf.TestUtils.course2;
import static school.hei.haapi.integration.conf.TestUtils.course3;
import static school.hei.haapi.integration.test_data.RetakeExamSessionTestData.session1;
import static school.hei.haapi.integration.test_data.RetakeExamSessionTestData.session2;
import static school.hei.haapi.integration.test_data.RetakeExamTestData.createRetakeExam;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.RetakeExam;
import school.hei.haapi.repository.RetakeExamRepository;
import school.hei.haapi.repository.RetakeExamSessionRepository;
import school.hei.haapi.service.GradeResultService;
import school.hei.haapi.service.RetakeExamService;

class RetakeExamServiceIT extends FacadeITMockedThirdParties {
  @MockBean private RetakeExamRepository retakeExamRepository;
  @MockBean private RetakeExamSessionRepository retakeExamSessionRepository;
  @MockBean private GradeResultService gradeResultService;
  @Autowired private RetakeExamService subject;

  @BeforeEach
  void setUp() {
    when(retakeExamSessionRepository.findById(session1().getId()))
        .thenReturn(Optional.of(session1()));
    when(retakeExamRepository
            .findRetakeExamByStudent_IdAndStatusIsNotInAndSession_DateToGreaterThan(
                any(), any(), any()))
        .thenReturn(List.of(retakeExamProg1(), retakeExamProg3(), retakeExamIa1()));
  }

  private static RetakeExam retakeExamProg1() {
    return createRetakeExam(
        axel(), school.hei.haapi.integration.test_data.CourseTestData.course1(), session1());
  }

  private static RetakeExam retakeExamProg3() {
    return createRetakeExam(
        axel(), school.hei.haapi.integration.test_data.CourseTestData.course2(), session1());
  }

  private static RetakeExam retakeExamIa1() {
    return createRetakeExam(
        axel(), school.hei.haapi.integration.test_data.CourseTestData.course3(), session2());
  }

  private static CourseResult courseResult1() {
    return new CourseResult().id("courseResultProg1_id").course(course1()).status(INCOMPLETE);
  }

  private static CourseResult courseResult2() {
    return new CourseResult().id("courseResultProg3_id").course(course2()).status(INCOMPLETE);
  }

  private static CourseResult courseResult3() {
    return new CourseResult().id("courseResultIa1_id").course(course3()).status(INCOMPLETE);
  }

  private static YearlyResult yearlyResult() {
    var yearlyResult = new YearlyResult();
    yearlyResult.setCourseResults(List.of(courseResult1(), courseResult2(), courseResult3()));
    return yearlyResult;
  }

  private static ResultSummary mockSummary() {
    var mockSummary = new ResultSummary();
    mockSummary.setYearlyResults(List.of(yearlyResult()));
    return mockSummary;
  }

  @Test
  void getAllStudentRetakeExams_with_two_future_sessions() {
    when(gradeResultService.getStudentResultSummary(any())).thenReturn(mockSummary());

    var retakeExams = subject.getStudentRetakeExams(session1().getId(), axel().getId());

    assertNotNull(retakeExams);
    assertEquals(2, retakeExams.size());
  }
}
