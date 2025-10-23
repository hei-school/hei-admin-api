package school.hei.haapi.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.INCOMPLETE;
import static school.hei.haapi.integration.test_data.CourseTestData.ia1;
import static school.hei.haapi.integration.test_data.CourseTestData.prog1;
import static school.hei.haapi.integration.test_data.CourseTestData.prog3;
import static school.hei.haapi.integration.test_data.RetakeExamSessionTestData.session1;
import static school.hei.haapi.integration.test_data.RetakeExamSessionTestData.session2;
import static school.hei.haapi.integration.test_data.RetakeExamTestData.retakeExamIa1;
import static school.hei.haapi.integration.test_data.RetakeExamTestData.retakeExamProg1;
import static school.hei.haapi.integration.test_data.RetakeExamTestData.retakeExamProg3;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.RetakeExam;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.RetakeExamRepository;
import school.hei.haapi.repository.RetakeExamSessionRepository;

class RetakeExamServiceTest extends FacadeITMockedThirdParties {
  @Autowired private CourseRepository courseRepository;
  @Autowired private RetakeExamRepository retakeExamRepository;
  @Autowired private RetakeExamSessionRepository retakeExamSessionRepository;
  @MockBean private GradeResultService gradeResultService;
  @Autowired private CourseMapper courseMapper;
  @Autowired private RetakeExamService subject;

  private static User axel;
  private static Course prog1;
  private static Course prog3;
  private static Course ia1;
  private static RetakeExamSession session1;
  private static RetakeExamSession session2;
  private static RetakeExam retakeExamProg1;
  private static RetakeExam retakeExamProg3;
  private static RetakeExam retakeExamIa1;

  @BeforeEach
  void setUp() {
    axel = axel();
    axel.setId("student1_id");
    prog1 = prog1();
    prog3 = prog3();
    ia1 = ia1();

    prog1.setId("prog1_id");
    prog3.setId("prog3_id");
    ia1.setId("ia1_id");

    session1 = session1();
    session2 = session2();
    retakeExamProg1 = retakeExamProg1();
    retakeExamProg1.setCourse(prog1);
    retakeExamProg1.setStudent(axel);
    retakeExamProg3 = retakeExamProg3();
    retakeExamProg3.setCourse(prog3);
    retakeExamProg3.setStudent(axel);
    retakeExamIa1 = retakeExamIa1();
    retakeExamIa1.setCourse(ia1);
    retakeExamIa1.setStudent(axel);
    courseRepository.saveAll(List.of(prog1, prog3, ia1));
    retakeExamSessionRepository.saveAll(List.of(session1, session2));
    retakeExamRepository.saveAll(List.of(retakeExamProg1, retakeExamProg3, retakeExamIa1));
  }

  private CourseResult courseResult1() {
    return new CourseResult()
        .id("courseResultProg1_id")
        .course(courseMapper.toRest(prog1))
        .status(INCOMPLETE);
  }

  private CourseResult courseResult2() {
    return new CourseResult()
        .id("courseResultProg3_id")
        .course(courseMapper.toRest(prog3))
        .status(INCOMPLETE);
  }

  private CourseResult courseResult3() {
    return new CourseResult()
        .id("courseResultIa1_id")
        .course(courseMapper.toRest(ia1))
        .status(INCOMPLETE);
  }

  @Test
  void getAllStudentRetakeExams_with_two_future_sessions() {
    var mockSummary = new ResultSummary();
    var yearlyResult = new YearlyResult();
    yearlyResult.setCourseResults(List.of(courseResult1(), courseResult2(), courseResult3()));
    mockSummary.setYearlyResults(List.of(yearlyResult));

    when(gradeResultService.getStudentResultSummary(any())).thenReturn(mockSummary);

    var retakeExams = subject.getStudentRetakeExams(session1().getId(), axel.getId());

    assertNotNull(retakeExams);
    assertEquals(2, retakeExams.size());
  }
}
