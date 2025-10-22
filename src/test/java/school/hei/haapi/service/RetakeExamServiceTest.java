package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.INCOMPLETE;
import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.VALIDATED;
import static school.hei.haapi.model.RetakeExamStatus.REGISTERED;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.RetakeExam;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.repository.RetakeExamRepository;
import school.hei.haapi.repository.dao.RetakeExamDao;

@ExtendWith(MockitoExtension.class)
class RetakeExamServiceTest {

  @Mock private RetakeExamRepository retakeExamRepository;
  @Mock private RetakeExamSessionService retakeExamSessionService;
  @Mock private GradeResultService gradeResultService;
  @Mock private CourseMapper courseMapper;
  @Mock private RetakeExamDao retakeExamDao;
  @Mock private PaginationFromPageAndPageSize paginationFromPageAndPageSize;
  @InjectMocks private RetakeExamService retakeExamService;

  private static final school.hei.haapi.model.User student1;
  private static final school.hei.haapi.model.Course course1;
  private static final school.hei.haapi.model.Course course2;
  private static final school.hei.haapi.model.Course course3;
  private static final RetakeExamSession session1;
  private static final RetakeExamSession session2;
  private static final RetakeExam retakeExam1;
  private static final RetakeExam retakeExam2;
  private static final CourseResult courseResult1;
  private static final CourseResult courseResult2;
  private static final CourseResult courseResult3;

  static {
    student1 = new school.hei.haapi.model.User();
    student1.setId("student1_id");
    student1.setFirstName("student1_first_name");

    course1 = new school.hei.haapi.model.Course();
    course1.setId("course1_id");
    course1.setName("course1_name");

    course2 = new school.hei.haapi.model.Course();
    course2.setId("course2_id");
    course2.setName("course2_name");

    course3 = new school.hei.haapi.model.Course();
    course3.setId("course3_id");
    course3.setName("course3_name");

    session1 = new RetakeExamSession();
    session1.setId("session1_id");
    session1.setTitle("session_2025_1");
    session1.setDateFrom(Instant.now().plus(2, ChronoUnit.DAYS));
    session1.setDateTo(Instant.now().plus(20, ChronoUnit.DAYS));

    session2 = new RetakeExamSession();
    session2.setId("session2_id");
    session2.setTitle("session_2025_2");
    session2.setDateFrom(Instant.now().plus(21, ChronoUnit.DAYS));
    session2.setDateTo(Instant.now().plus(30, ChronoUnit.DAYS));

    retakeExam1 = new RetakeExam();
    retakeExam1.setId("retake1_id");
    retakeExam1.setCourse(course1);
    retakeExam1.setStudent(student1);
    retakeExam1.setSession(session1);
    retakeExam1.setStatus(REGISTERED);

    retakeExam2 = new RetakeExam();
    retakeExam2.setId("retake2_id");
    retakeExam2.setCourse(course2);
    retakeExam2.setStudent(student1);
    retakeExam2.setSession(session2);
    retakeExam2.setStatus(REGISTERED);

    courseResult1 = new CourseResult();
    courseResult1.setId("courseResult1_id");
    courseResult1.setCourse(new Course().id(course1.getId()).name(course1.getName()));
    courseResult1.setStatus(INCOMPLETE);

    courseResult2 = new CourseResult();
    courseResult2.setId("courseResult2_id");
    courseResult2.setCourse(new Course().id(course2.getId()).name(course2.getName()));
    courseResult2.setStatus(INCOMPLETE);

    courseResult3 = new CourseResult();
    courseResult3.setId("courseResult3_id");
    courseResult3.setCourse(new Course().id(course3.getId()).name(course3.getName()));
    courseResult3.setStatus(INCOMPLETE);
  }

  @Test
  void crupdateRetakeExams_should_save_all_retake_exams() {
    var retakeExams = List.of(retakeExam1, retakeExam2);

    when(retakeExamRepository.saveAll(retakeExams)).thenReturn(retakeExams);

    List<RetakeExam> result = retakeExamService.crupdateRetakeExams(retakeExams);

    assertEquals(2, result.size());
    verify(retakeExamRepository).saveAll(retakeExams);
  }

  @Test
  void getAllStudentRetakeExams_with_two_future_sessions() {
    ResultSummary mockSummary = new ResultSummary();
    YearlyResult yearlyResult = new YearlyResult();
    yearlyResult.setCourseResults(List.of(courseResult1, courseResult2, courseResult3));
    mockSummary.setYearlyResults(List.of(yearlyResult));

    when(gradeResultService.getStudentResultSummary(any())).thenReturn(mockSummary);
    when(retakeExamRepository.findActiveRetakeExamsInFutureSessions(any(), any(), any()))
        .thenReturn(List.of(retakeExam1, retakeExam2));
    when(retakeExamSessionService.getById(any())).thenReturn(session1);

    var retakeExams = retakeExamService.getStudentRetakeExams(session1.getId(), student1.getId());
    System.out.println(retakeExams);
    assertNotNull(retakeExams);
    assertEquals(2, retakeExams.size());
  }

  @Test
  void courseResultAndSessionToRetake_with_valid_course_should_create_retake_exam() {
    Course validCourse = new Course();
    validCourse.setId("course1");
    CourseResult courseResult = new CourseResult();
    courseResult.setCourse(validCourse);
    courseResult.setStatus(INCOMPLETE);

    RetakeExamSession session = new RetakeExamSession();
    session.setId("session1");

    var domainCourse = new school.hei.haapi.model.Course();
    domainCourse.setId("course1");
    when(courseMapper.toDomain(validCourse)).thenReturn(domainCourse);

    RetakeExam result = retakeExamService.courseResultAndSessionToRetake(courseResult, session);

    assertNotNull(result);
    assertEquals(domainCourse, result.getCourse());
    assertEquals(session, result.getSession());
  }

  @Test
  void courseResultAndSessionToRetake_with_null_course_should_throw_exception() {
    CourseResult courseResultWithNullCourse = new CourseResult();
    courseResultWithNullCourse.setCourse(null);
    courseResultWithNullCourse.setStatus(INCOMPLETE);

    RetakeExamSession session = new RetakeExamSession();
    session.setId("session1");

    assertThrows(
        IllegalStateException.class,
        () ->
            retakeExamService.courseResultAndSessionToRetake(courseResultWithNullCourse, session));
  }

  @Test
  void getCourseResultToRetake_should_return_only_incomplete_courses() {
    String studentId = student1.getId();

    Course course1 = new Course();
    course1.setId("course1");
    Course course2 = new Course();
    course2.setId("course2");

    CourseResult incompleteCourse = new CourseResult();
    incompleteCourse.setCourse(course1);
    incompleteCourse.setStatus(INCOMPLETE);
    CourseResult completeCourse = new CourseResult();
    completeCourse.setCourse(course2);
    completeCourse.setStatus(VALIDATED);

    YearlyResult yearlyResult = new YearlyResult();
    yearlyResult.setCourseResults(List.of(incompleteCourse, completeCourse));

    ResultSummary resultSummary = new ResultSummary();
    resultSummary.setYearlyResults(List.of(yearlyResult));

    when(gradeResultService.getStudentResultSummary(studentId)).thenReturn(resultSummary);

    List<CourseResult> result = retakeExamService.getCourseResultToRetake(studentId);

    assertEquals(1, result.size());
    assertEquals(INCOMPLETE, result.getFirst().getStatus());
    assertEquals("course1", result.getFirst().getCourse().getId());
  }

  @Test
  void getCourseResultToRetake_should_throw_exception_when_yearlyResults_is_null() {
    String studentId = student1.getId();

    ResultSummary resultSummary = new ResultSummary();
    resultSummary.setYearlyResults(null);

    when(gradeResultService.getStudentResultSummary(studentId)).thenReturn(resultSummary);

    assertThrows(
        NullPointerException.class, () -> retakeExamService.getCourseResultToRetake(studentId));
  }

  @Test
  void getCourseResultToRetake_should_handle_null_course_results() {
    String studentId = student1.getId();

    YearlyResult yearlyResult = new YearlyResult();
    yearlyResult.setCourseResults(null);

    ResultSummary resultSummary = new ResultSummary();
    resultSummary.setYearlyResults(List.of(yearlyResult));

    when(gradeResultService.getStudentResultSummary(studentId)).thenReturn(resultSummary);

    List<CourseResult> result = retakeExamService.getCourseResultToRetake(studentId);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getAllRetakeExamCoursesBySessionId_should_return_distinct_sorted_courses() {
    String sessionId = session1.getId();
    PageFromOne page = new PageFromOne(1);
    BoundedPageSize pageSize = new BoundedPageSize(10);
    Pageable pageable = PageRequest.of(0, 10);

    var course1 = new school.hei.haapi.model.Course();
    course1.setCode("LV");
    var course2 = new school.hei.haapi.model.Course();
    course2.setCode("MATH");

    RetakeExam retakeExam1 = new RetakeExam();
    retakeExam1.setCourse(course1);
    RetakeExam retakeExam2 = new RetakeExam();
    retakeExam2.setCourse(course2);
    RetakeExam retakeExam3 = new RetakeExam();
    retakeExam3.setCourse(course1);

    when(paginationFromPageAndPageSize.apply(page, pageSize)).thenReturn(pageable);
    when(retakeExamDao.filterByCriteria(sessionId, null, null, null, null, null, pageable))
        .thenReturn(List.of(retakeExam1, retakeExam2, retakeExam3));

    List<school.hei.haapi.model.Course> result =
        retakeExamService.getAllRetakeExamCoursesBySessionId(sessionId, null, page, pageSize);

    assertEquals(2, result.size());
    assertEquals("LV", result.getFirst().getCode());
    assertEquals("MATH", result.get(1).getCode());
  }

  @Test
  void getAllRetakeExamParticipantByCourseAndBySessionId_should_return_students() {
    String sessionId = session1.getId();
    String courseId = "course1";
    PageFromOne page = new PageFromOne(1);
    BoundedPageSize pageSize = new BoundedPageSize(10);
    Pageable pageable = PageRequest.of(0, 10);

    var student1 = new school.hei.haapi.model.User();
    student1.setId("student1");
    var student2 = new school.hei.haapi.model.User();
    student2.setId("student2");

    RetakeExam retakeExam1 = new RetakeExam();
    retakeExam1.setStudent(student1);
    RetakeExam retakeExam2 = new RetakeExam();
    retakeExam2.setStudent(student2);

    when(paginationFromPageAndPageSize.apply(page, pageSize)).thenReturn(pageable);
    when(retakeExamDao.filterByCriteria(sessionId, null, null, courseId, null, null, pageable))
        .thenReturn(List.of(retakeExam1, retakeExam2));

    List<school.hei.haapi.model.User> result =
        retakeExamService.getAllRetakeExamParticipantByCourseAndBySessionId(
            sessionId, courseId, null, page, pageSize);

    assertEquals(2, result.size());
    assertEquals("student1", result.getFirst().getId());
    assertEquals("student2", result.get(1).getId());
  }
}
