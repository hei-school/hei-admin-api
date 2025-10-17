package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.INCOMPLETE;
import static school.hei.haapi.endpoint.rest.model.CourseResultStatus.VALIDATED;

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
import school.hei.haapi.model.RetakeExamStatus;
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

  @Test
  void crupdateRetakeExams_should_save_all_retake_exams() {
    RetakeExam retakeExam1 = new RetakeExam();
    retakeExam1.setId("retake1");
    RetakeExam retakeExam2 = new RetakeExam();
    retakeExam2.setId("retake2");
    List<RetakeExam> retakeExams = List.of(retakeExam1, retakeExam2);

    when(retakeExamRepository.saveAll(retakeExams)).thenReturn(retakeExams);

    List<RetakeExam> result = retakeExamService.crupdateRetakeExams(retakeExams);

    assertEquals(2, result.size());
    verify(retakeExamRepository).saveAll(retakeExams);
  }

  @Test
  void getStudentRetakeExams_with_expired_session_should_return_only_existing() {
    String studentId = "student1";
    String sessionId = "expired_session";

    RetakeExamSession expiredSession = new RetakeExamSession();
    expiredSession.setId(sessionId);
    expiredSession.setDateTo(Instant.now().minus(1, ChronoUnit.DAYS));

    RetakeExam existingRetake = new RetakeExam();
    existingRetake.setId("existing_retake");

    when(retakeExamSessionService.getById(sessionId)).thenReturn(expiredSession);
    when(retakeExamRepository.findRetakeExamsBySession_IdAndStudent_Id(sessionId, studentId))
        .thenReturn(List.of(existingRetake));

    List<RetakeExam> result = retakeExamService.getStudentRetakeExams(sessionId, studentId);

    assertEquals(1, result.size());
    assertEquals("existing_retake", result.get(0).getId());
    verify(retakeExamRepository, never())
        .findRetakeExamsByStudent_IdAndSession_DateFromAfter(anyString(), any());
  }

  @Test
  void getStudentRetakeExams_with_active_session_should_generate_new_retake_exams() {
    String studentId = "student1";
    String sessionId = "active_session";

    RetakeExamSession activeSession = new RetakeExamSession();
    activeSession.setId(sessionId);
    activeSession.setDateTo(Instant.now().plus(1, ChronoUnit.DAYS));

    Course course1 = new Course();
    course1.setId("course1");
    Course course2 = new Course();
    course2.setId("course2");

    CourseResult incompleteCourse1 = new CourseResult();
    incompleteCourse1.setCourse(course1);
    incompleteCourse1.setStatus(INCOMPLETE);
    CourseResult incompleteCourse2 = new CourseResult();
    incompleteCourse2.setCourse(course2);
    incompleteCourse2.setStatus(INCOMPLETE);

    YearlyResult yearlyResult = new YearlyResult();
    yearlyResult.setCourseResults(List.of(incompleteCourse1, incompleteCourse2));

    ResultSummary resultSummary = new ResultSummary();
    resultSummary.setYearlyResults(List.of(yearlyResult));

    when(retakeExamSessionService.getById(sessionId)).thenReturn(activeSession);
    when(retakeExamRepository.findRetakeExamsBySession_IdAndStudent_Id(sessionId, studentId))
        .thenReturn(List.of());
    when(retakeExamRepository.findRetakeExamsByStudent_IdAndSession_DateFromAfter(
            anyString(), any()))
        .thenReturn(List.of());
    when(gradeResultService.getStudentResultSummary(studentId)).thenReturn(resultSummary);

    school.hei.haapi.model.Course domainCourse1 = new school.hei.haapi.model.Course();
    domainCourse1.setId("course1");
    school.hei.haapi.model.Course domainCourse2 = new school.hei.haapi.model.Course();
    domainCourse2.setId("course2");
    when(courseMapper.toDomain(course1)).thenReturn(domainCourse1);
    when(courseMapper.toDomain(course2)).thenReturn(domainCourse2);

    List<RetakeExam> result = retakeExamService.getStudentRetakeExams(sessionId, studentId);

    assertNotNull(result);
    assertEquals(2, result.size());
  }

  @Test
  void getStudentRetakeExams_should_filter_existing_retake_exams() {
    String studentId = "student1";
    String sessionId = "active_session";

    RetakeExamSession activeSession = new RetakeExamSession();
    activeSession.setId(sessionId);
    activeSession.setDateTo(Instant.now().plus(1, ChronoUnit.DAYS));

    Course course1 = new Course();
    course1.setId("course1");
    Course course2 = new Course();
    course2.setId("course2");

    CourseResult incompleteCourse1 = new CourseResult();
    incompleteCourse1.setCourse(course1);
    incompleteCourse1.setStatus(INCOMPLETE);
    CourseResult incompleteCourse2 = new CourseResult();
    incompleteCourse2.setCourse(course2);
    incompleteCourse2.setStatus(INCOMPLETE);

    YearlyResult yearlyResult = new YearlyResult();
    yearlyResult.setCourseResults(List.of(incompleteCourse1, incompleteCourse2));

    ResultSummary resultSummary = new ResultSummary();
    resultSummary.setYearlyResults(List.of(yearlyResult));

    school.hei.haapi.model.Course domainCourse1 = new school.hei.haapi.model.Course();
    domainCourse1.setId("course1");
    school.hei.haapi.model.Course domainCourse2 = new school.hei.haapi.model.Course();
    domainCourse2.setId("course2");

    school.hei.haapi.model.User student = new school.hei.haapi.model.User();
    student.setId("student1");

    RetakeExam existingRetakeInOtherSession = new RetakeExam();
    existingRetakeInOtherSession.setCourse(domainCourse2);
    existingRetakeInOtherSession.setStudent(student);

    when(retakeExamSessionService.getById(sessionId)).thenReturn(activeSession);
    when(retakeExamRepository.findRetakeExamsBySession_IdAndStudent_Id(sessionId, studentId))
        .thenReturn(List.of());
    when(retakeExamRepository.findRetakeExamsByStudent_IdAndSession_DateFromAfter(
            anyString(), any()))
        .thenReturn(List.of(existingRetakeInOtherSession));
    when(gradeResultService.getStudentResultSummary(studentId)).thenReturn(resultSummary);
    when(courseMapper.toDomain(course1)).thenReturn(domainCourse1);
    when(courseMapper.toDomain(course2)).thenReturn(domainCourse2);

    List<RetakeExam> result = retakeExamService.getStudentRetakeExams(sessionId, studentId);

    assertEquals(1, result.size());
    assertEquals("course1", result.get(0).getCourse().getId());
  }

  @Test
  void isRetakeIn_should_return_true_when_retake_exists() {
    String studentId = "student1";

    school.hei.haapi.model.Course course = new school.hei.haapi.model.Course();
    course.setId("course1");
    school.hei.haapi.model.User student = new school.hei.haapi.model.User();
    student.setId("student1");

    RetakeExam newExam = new RetakeExam();
    newExam.setCourse(course);
    newExam.setStudent(student);

    RetakeExam existingExam = new RetakeExam();
    existingExam.setCourse(course);
    existingExam.setStudent(student);

    List<RetakeExam> existingExams = List.of(existingExam);

    boolean result = retakeExamService.isRetakeIn(newExam, existingExams, studentId);

    assertTrue(result);
  }

  @Test
  void isRetakeIn_should_return_false_when_no_retake_exists() {
    String studentId = "student1";

    school.hei.haapi.model.Course course1 = new school.hei.haapi.model.Course();
    course1.setId("course1");
    school.hei.haapi.model.Course course2 = new school.hei.haapi.model.Course();
    course2.setId("course2");
    school.hei.haapi.model.User student = new school.hei.haapi.model.User();
    student.setId("student1");

    RetakeExam newExam = new RetakeExam();
    newExam.setCourse(course2);
    newExam.setStudent(student);

    RetakeExam existingExam = new RetakeExam();
    existingExam.setCourse(course1);
    existingExam.setStudent(student);

    List<RetakeExam> existingExams = List.of(existingExam);

    boolean result = retakeExamService.isRetakeIn(newExam, existingExams, studentId);

    assertFalse(result);
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

    school.hei.haapi.model.Course domainCourse = new school.hei.haapi.model.Course();
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
    String studentId = "student1";

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
    assertEquals(INCOMPLETE, result.get(0).getStatus());
    assertEquals("course1", result.get(0).getCourse().getId());
  }

  @Test
  void getCourseResultToRetake_should_throw_exception_when_yearlyResults_is_null() {
    String studentId = "student1";

    ResultSummary resultSummary = new ResultSummary();
    resultSummary.setYearlyResults(null);

    when(gradeResultService.getStudentResultSummary(studentId)).thenReturn(resultSummary);

    assertThrows(
        NullPointerException.class, () -> retakeExamService.getCourseResultToRetake(studentId));
  }

  @Test
  void getCourseResultToRetake_should_handle_null_course_results() {
    String studentId = "student1";

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
  void getAllRetakeExamBySessionId_should_call_dao() {
    String sessionId = "session1";
    List<RetakeExamStatus> statuses = List.of(RetakeExamStatus.REGISTERED);
    PageFromOne page = new PageFromOne(1);
    BoundedPageSize pageSize = new BoundedPageSize(10);
    Pageable pageable = PageRequest.of(0, 10);

    RetakeExam retakeExam = new RetakeExam();
    when(retakeExamDao.filterByCriteria(sessionId, null, null, null, null, statuses, pageable))
        .thenReturn(List.of(retakeExam));

    List<RetakeExam> result =
        retakeExamService.getAllRetakeExamBySessionId(sessionId, statuses, page, pageSize);

    assertEquals(1, result.size());
  }

  @Test
  void getAllRetakeExamCoursesBySessionId_should_return_distinct_sorted_courses() {
    String sessionId = "session1";
    PageFromOne page = new PageFromOne(1);
    BoundedPageSize pageSize = new BoundedPageSize(10);
    Pageable pageable = PageRequest.of(0, 10);

    school.hei.haapi.model.Course course1 = new school.hei.haapi.model.Course();
    course1.setCode("BIO");
    school.hei.haapi.model.Course course2 = new school.hei.haapi.model.Course();
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
    assertEquals("BIO", result.get(0).getCode());
    assertEquals("MATH", result.get(1).getCode());
  }

  @Test
  void getAllRetakeExamParticipantByCourseAndBySessionId_should_return_students() {
    String sessionId = "session1";
    String courseId = "course1";
    PageFromOne page = new PageFromOne(1);
    BoundedPageSize pageSize = new BoundedPageSize(10);
    Pageable pageable = PageRequest.of(0, 10);

    school.hei.haapi.model.User student1 = new school.hei.haapi.model.User();
    student1.setId("student1");
    school.hei.haapi.model.User student2 = new school.hei.haapi.model.User();
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
    assertEquals("student1", result.get(0).getId());
    assertEquals("student2", result.get(1).getId());
  }
}
