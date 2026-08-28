package school.hei.haapi.integration;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.INVALIDATED;
import static school.hei.haapi.endpoint.rest.model.RetakeExamStatus.CANCELED;
import static school.hei.haapi.endpoint.rest.model.RetakeExamStatus.REGISTERED;
import static school.hei.haapi.endpoint.rest.model.RetakeExamStatus.TO_CANCEL;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.CourseTestData.toRest;
import static school.hei.haapi.integration.testData.RequestRetakeExam.cancelRequest;
import static school.hei.haapi.integration.testData.RequestRetakeExam.toCancel;
import static school.hei.haapi.integration.testData.RetakeExamSessionTestData.passedSession;
import static school.hei.haapi.integration.testData.RetakeExamSessionTestData.session1;
import static school.hei.haapi.integration.testData.RetakeExamSessionTestData.session2;
import static school.hei.haapi.integration.testData.RetakeExamTestData.createRetakeExam;
import static school.hei.haapi.integration.testData.StaffTestData.adminMialy;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;
import static school.hei.haapi.integration.testData.StudentTestData.tolojanahary;

import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
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
import school.hei.haapi.model.Course;
import school.hei.haapi.model.RetakeExam;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.model.RetakeExamStatus;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.RetakeExamRepository;
import school.hei.haapi.repository.RetakeExamSessionRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.RetakeExamDao;
import school.hei.haapi.service.GradeResultService;

public class RetakeExamIT extends FacadeITMockedThirdParties {
  @MockBean GradeResultService gradeResultService;
  @Autowired private RetakeExamDao retakeExamDao;
  @Autowired private UserRepository userRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private RetakeExamSessionRepository retakeExamSessionRepository;
  @Autowired private RetakeExamRepository retakeExamRepository;

  private User studentAxel;
  private User studentFreddy;
  private User studentTolojanahary;
  private User adminUser;

  // codes chosen so the alphabetical order is IA2 < PROG1 < PROG3
  private Course courseIa2;
  private Course courseProg1;
  private Course courseProg3;

  private RetakeExamSession currentSession;
  private RetakeExamSession otherSession;
  private RetakeExamSession passedRetakeSession;

  private RetakeExam axelCanceled;
  private RetakeExam freddyToCancel;
  private RetakeExam tolojanaharyRegistered;
  private RetakeExam freddyCanceledOnProg1;
  private RetakeExam tolojanaharyInPassedSession;

  private String axelToken;
  private String adminToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    studentFreddy = userRepository.save(freddy());
    studentTolojanahary = userRepository.save(tolojanahary());
    adminUser = userRepository.save(adminMialy());

    // course.code is unique across the schema: prefix fixes the alphabetical order, suffix the
    // uniqueness
    courseIa2 = courseRepository.save(aCourse("AAA-" + randomUUID(), "Implemented IA"));
    courseProg1 = courseRepository.save(aCourse("MMM-" + randomUUID(), "Algorithmics"));
    courseProg3 = courseRepository.save(aCourse("ZZZ-" + randomUUID(), "Advanced OOP"));

    currentSession = retakeExamSessionRepository.save(session1());
    otherSession = retakeExamSessionRepository.save(session2());
    passedRetakeSession = retakeExamSessionRepository.save(passedSession());

    axelCanceled =
        retakeExamRepository.save(
            createRetakeExam(
                studentAxel, courseProg1, currentSession, RetakeExamStatus.CANCELED, now()));
    freddyToCancel =
        retakeExamRepository.save(
            createRetakeExam(
                studentFreddy, courseProg3, otherSession, RetakeExamStatus.TO_CANCEL, now()));
    tolojanaharyRegistered =
        retakeExamRepository.save(
            createRetakeExam(
                studentTolojanahary, courseIa2, otherSession, RetakeExamStatus.REGISTERED, now()));
    freddyCanceledOnProg1 =
        retakeExamRepository.save(
            createRetakeExam(
                studentFreddy, courseProg1, otherSession, RetakeExamStatus.CANCELED, now()));
    tolojanaharyInPassedSession =
        retakeExamRepository.save(
            createRetakeExam(
                studentTolojanahary,
                courseIa2,
                passedRetakeSession,
                RetakeExamStatus.REGISTERED,
                now().minus(60, DAYS)));
  }

  @BeforeEach
  void setup() {
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    adminToken = tokenFor(casdoorAuthServiceMock, adminUser);

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
                                        .course(toRest(courseProg1, L1))
                                        .status(CourseResultStatus.INCOMPLETE)
                                        .weightedAverage(ONE))))));
  }

  @AfterEach
  void tearDown() {
    retakeExamRepository.deleteAll(
        retakeExamRepository.findAll().stream()
            .filter(r -> ownedSessionIds().contains(r.getSession().getId()))
            .toList());
    retakeExamSessionRepository.deleteAll(
        List.of(currentSession, otherSession, passedRetakeSession));
    courseRepository.deleteAll(List.of(courseIa2, courseProg1, courseProg3));
    userRepository.deleteAll(List.of(studentAxel, studentFreddy, studentTolojanahary, adminUser));
  }

  private List<String> ownedSessionIds() {
    return List.of(currentSession.getId(), otherSession.getId(), passedRetakeSession.getId());
  }

  private RetakeExamApi apiAs(String token) {
    return new RetakeExamApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private static Course aCourse(String code, String name) {
    return Course.builder()
        .id(randomUUID().toString())
        .code(code)
        .name(name)
        .credits(6)
        .studentLevel(L1)
        .totalHours(80)
        .build();
  }

  @Test
  void student_create_retake_exam_ok() throws ApiException {
    var retakeExam = new CrupdateRetakeExam();
    retakeExam.setStudentId(studentAxel.getId());
    retakeExam.setCourseId(courseProg1.getId());
    retakeExam.setSessionId(otherSession.getId());
    retakeExam.setStatus(TO_CANCEL);

    var created =
        apiAs(axelToken).createOrUpdateRetakeExam(otherSession.getId(), List.of(retakeExam));

    assertNotNull(created);
    assertEquals(1, created.size());
    var first = created.getFirst();
    assertEquals(otherSession.getId(), first.getSession().getId());
    assertEquals(courseProg1.getId(), first.getCourse().getId());
    assertEquals(studentAxel.getId(), first.getStudentIdentifier().getId());

    retakeExamRepository.deleteById(first.getId());
  }

  @Test
  void admin_read_all_retake_exams_ok() throws ApiException {
    var retakeExams = apiAs(adminToken).getAllRetakeExams(null, null, null, null, 1, 100);

    assertNotNull(retakeExams);
    assertTrue(
        retakeExams.stream().anyMatch(r -> axelCanceled.getId().equals(r.getId())),
        "should list the retake exams of this test");
    assertNotNull(retakeExams.getFirst().getStudentIdentifier());
  }

  @Test
  void filter_retake_exam_by_status_ok() throws ApiException {
    var filtered =
        apiAs(adminToken).getAllRetakeExams(List.of(TO_CANCEL), null, null, null, 1, 100);

    assertNotNull(filtered);
    assertTrue(filtered.stream().allMatch(r -> TO_CANCEL.equals(r.getStatus())));
    assertTrue(filtered.stream().anyMatch(r -> freddyToCancel.getId().equals(r.getId())));
  }

  @Test
  void get_all_retake_exam_courses_ok() throws ApiException {
    var courses =
        apiAs(adminToken).getRetakeExamCoursesBySessionId(otherSession.getId(), null, 1, 15);

    assertNotNull(courses);
    assertEquals(3, courses.size());
    // ordered by code
    assertEquals(courseIa2.getCode(), courses.getFirst().getCode());
    assertEquals(courseProg1.getCode(), courses.get(1).getCode());
    assertEquals(courseProg3.getCode(), courses.get(2).getCode());
  }

  @Test
  void filter_retake_exam_course_by_course_code_ok() throws ApiException {
    var courses =
        apiAs(adminToken)
            .getRetakeExamCoursesBySessionId(otherSession.getId(), courseIa2.getCode(), 1, 15);

    assertNotNull(courses);
    assertEquals(1, courses.size());
    assertEquals("Implemented IA", courses.getFirst().getName());
    assertEquals(courseIa2.getCode(), courses.getFirst().getCode());
  }

  @Test
  void get_all_retake_exam_participants_of_course_ok() throws ApiException {
    var participants =
        apiAs(adminToken)
            .getRetakeExamParticipantByCourseIdAndSessionId(
                otherSession.getId(), courseIa2.getId(), null, 1, 15);

    assertNotNull(participants);
    assertEquals(1, participants.size());
    assertEquals(
        studentTolojanahary.getId(), participants.getFirst().getStudentIdentifier().getId());
  }

  @Test
  void canceled_retake_exams_are_not_listed_as_participants() throws ApiException {
    // freddy is registered on courseProg1 for this session, but his retake exam is CANCELED
    var participants =
        apiAs(adminToken)
            .getRetakeExamParticipantByCourseIdAndSessionId(
                otherSession.getId(), courseProg1.getId(), null, 1, 15);

    assertNotNull(participants);
    assertTrue(participants.isEmpty());
  }

  @Test
  void filter_retake_exam_participants_by_student_ref_ok() throws ApiException {
    var participants =
        apiAs(adminToken)
            .getRetakeExamParticipantByCourseIdAndSessionId(
                otherSession.getId(), courseIa2.getId(), studentTolojanahary.getRef(), 1, 15);

    assertNotNull(participants);
    assertEquals(
        studentTolojanahary.getRef(), participants.getFirst().getStudentIdentifier().getRef());
    assertEquals(
        studentTolojanahary.getId(), participants.getFirst().getStudentIdentifier().getId());
    assertEquals(
        studentTolojanahary.getFirstName(),
        participants.getFirst().getStudentIdentifier().getFirstName());
  }

  @Test
  void filter_retake_exam_by_criteria_ok() {
    var pageable = PageRequest.of(0, 100);

    var retakeExams =
        retakeExamDao.filterByCriteria(
            null, null, null, null, null, List.of(RetakeExamStatus.REGISTERED), pageable);

    assertNotNull(retakeExams);
    assertTrue(
        retakeExams.stream().allMatch(r -> RetakeExamStatus.REGISTERED.equals(r.getStatus())));
    assertTrue(
        retakeExams.stream().anyMatch(r -> tolojanaharyRegistered.getId().equals(r.getId())));
  }

  @Test
  void get_retake_exam_in_passed_session_ok() throws ApiException {
    var retakeExams =
        apiAs(axelToken)
            .getStudentRetakeExamBySession(
                studentTolojanahary.getId(), passedRetakeSession.getId(), null, null);

    assertNotNull(retakeExams);
    assertEquals(passedRetakeSession.getId(), retakeExams.getFirst().getSession().getId());
    assertEquals(courseIa2.getId(), retakeExams.getFirst().getCourse().getId());
  }

  @Test
  void filter_retake_exam_by_student_ref_ok() throws ApiException {
    var retakeExams =
        apiAs(adminToken).getAllRetakeExams(null, studentFreddy.getRef(), null, null, 1, 100);

    assertNotNull(retakeExams);
    assertEquals(2, retakeExams.size());
    assertTrue(
        retakeExams.stream()
            .allMatch(
                r ->
                    studentFreddy
                        .getRef()
                        .equals(Objects.requireNonNull(r.getStudentIdentifier()).getRef())));
  }

  @Test
  void request_to_cancel_retake_exam_by_student_ok() throws ApiException {
    var retakeExams =
        apiAs(axelToken)
            .requestToCancelRetakeExams(
                List.of(
                    cancelRequest(tolojanaharyRegistered.getId(), "Don't have money"),
                    cancelRequest(tolojanaharyInPassedSession.getId(), "I'll visit my parents")));

    assertNotNull(retakeExams);
    assertEquals(2, retakeExams.size());
    assertEquals(TO_CANCEL, retakeExams.getFirst().getStatus());
    assertEquals(TO_CANCEL, retakeExams.getLast().getStatus());
  }

  @Test
  void cancel_retake_exam_by_admin_ok() throws ApiException {
    var retakeExam = apiAs(adminToken).cancelRetakeExams(List.of(toCancel(freddyToCancel.getId())));

    assertNotNull(retakeExam);
    assertEquals(CANCELED, retakeExam.getFirst().getStatus());
  }

  @Test
  void reject_request_to_cancel_retake_exam_by_student_ok() throws ApiException {
    var retakeExams =
        apiAs(adminToken)
            .rejectToCancelRetakeExamRequests(
                List.of(
                    cancelRequest(freddyToCancel.getId(), "you can pay it latter"),
                    cancelRequest(
                        tolojanaharyRegistered.getId(),
                        "you can go after the retake exam session")));

    assertNotNull(retakeExams);
    assertEquals(2, retakeExams.size());
    assertEquals(REGISTERED, retakeExams.getFirst().getStatus());
    assertEquals(REGISTERED, retakeExams.getLast().getStatus());
  }

  @Test
  void should_see_all_all_exams_INCOMPLETED() throws ApiException {
    var results =
        apiAs(axelToken)
            .getListStudentRetakeExams(studentAxel.getId(), CourseResultStatus.INCOMPLETE);

    assertNotNull(results, "Results should be not null");
  }
}
