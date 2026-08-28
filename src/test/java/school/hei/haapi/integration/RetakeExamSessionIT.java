package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M1;
import static school.hei.haapi.integration.conf.ApiAssertions.assertBadRequestException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.testData.RetakeExamSessionTestData.passedSession;
import static school.hei.haapi.integration.testData.RetakeExamSessionTestData.session1;
import static school.hei.haapi.integration.testData.RetakeExamSessionTestData.session2;
import static school.hei.haapi.integration.testData.RetakeExamSessionTestData.session3;
import static school.hei.haapi.integration.testData.RetakeExamSessionTestData.sessionWithWrongDate;
import static school.hei.haapi.integration.testData.StaffTestData.adminMialy;
import static school.hei.haapi.integration.testData.StudentTestData.axel;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import school.hei.haapi.endpoint.rest.api.RetakeExamApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.RetakeExamSessionMapper;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.RetakeExamSessionRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.RetakeExamSessionDao;

public class RetakeExamSessionIT extends FacadeITMockedThirdParties {
  @Autowired private RetakeExamSessionMapper retakeExamSessionMapper;
  @Autowired private RetakeExamSessionRepository retakeExamSessionRepository;
  @Autowired private RetakeExamSessionDao retakeExamSessionDao;
  @Autowired private UserRepository userRepository;

  private User adminUser;
  private User studentAxel;
  private RetakeExamSession futureM2Session;
  private RetakeExamSession futureM1M2Session;
  private RetakeExamSession futureL2L3Session;
  private RetakeExamSession passedL1L2Session;
  private final List<String> ownedSessionIds = new ArrayList<>();

  private String adminToken;
  private String studentToken;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {

    futureM2Session = retakeExamSessionRepository.save(session1());
    futureM1M2Session = retakeExamSessionRepository.save(session2());
    futureL2L3Session = retakeExamSessionRepository.save(session3());
    passedL1L2Session = retakeExamSessionRepository.save(passedSession());
    ownedSessionIds.addAll(
        List.of(
            futureM2Session.getId(),
            futureM1M2Session.getId(),
            futureL2L3Session.getId(),
            passedL1L2Session.getId()));

    adminUser = userRepository.save(adminMialy());
    studentAxel = userRepository.save(axel());
    adminToken = tokenFor(casdoorAuthServiceMock, adminUser);
    studentToken = tokenFor(casdoorAuthServiceMock, studentAxel);
  }

  @AfterEach
  void tearDown() {
    retakeExamSessionRepository.deleteAllById(ownedSessionIds);
    ownedSessionIds.clear();
    userRepository.deleteAll(List.of(adminUser, studentAxel));
  }

  private RetakeExamApi apiAs(String token) {
    return new RetakeExamApi(anApiClient(token));
  }

  private static List<String> idsOf(
      List<school.hei.haapi.endpoint.rest.model.RetakeExamSession> sessions) {
    return sessions.stream().map(session -> session.getId()).toList();
  }

  @Test
  void read_all_retake_exam_sessions_by_student_ok() throws ApiException {
    var retakeExamSessions =
        apiAs(studentToken).getRetakeExamSessions(null, null, null, null, 1, 100);

    assertNotNull(retakeExamSessions);
    assertTrue(idsOf(retakeExamSessions).containsAll(ownedSessionIds));
  }

  @Test
  void read_all_retake_exam_sessions_by_admin_ok() throws ApiException {
    var retakeExamSessions =
        apiAs(adminToken).getRetakeExamSessions(null, null, null, null, 1, 100);

    assertNotNull(retakeExamSessions);
    assertTrue(idsOf(retakeExamSessions).containsAll(ownedSessionIds));
  }

  @Test
  void filter_retake_exam_session_by_admin_ok() throws ApiException {
    var retakeExamFiltered =
        apiAs(adminToken)
            .getRetakeExamSessions(futureM2Session.getTitle(), null, null, null, null, null);

    assertNotNull(retakeExamFiltered);
    assertEquals(1, retakeExamFiltered.size());
    assertEquals(futureM2Session.getId(), retakeExamFiltered.getFirst().getId());
    assertEquals(futureM2Session.getTitle(), retakeExamFiltered.getFirst().getTitle());
  }

  @Test
  void save_retake_exam_session_by_admin_ok() throws ApiException {
    var toSave = session1();

    var created =
        apiAs(adminToken).createOrUpdateRetakeExamSessions(retakeExamSessionMapper.toRest(toSave));

    assertNotNull(created);
    // a creation mints its own id; only an update keeps the one it was given
    assertNotNull(created.getId());
    assertEquals(toSave.getTitle(), created.getTitle());

    ownedSessionIds.add(created.getId());
  }

  @Test
  void update_retake_exam_session_by_admin_ok() throws ApiException {
    var toUpdate = retakeExamSessionMapper.toRest(futureM2Session).title("session1 renamed");

    var updated = apiAs(adminToken).createOrUpdateRetakeExamSessions(toUpdate);

    assertEquals(futureM2Session.getId(), updated.getId());
    assertEquals("session1 renamed", updated.getTitle());
  }

  @Test
  void save_retake_exam_session_with_wrong_date_ko() {
    var api = apiAs(adminToken);
    var sessionWithWrongDate = sessionWithWrongDate();

    assertBadRequestException(
        "Session start date must be before end date",
        () ->
            api.createOrUpdateRetakeExamSessions(
                retakeExamSessionMapper.toRest(sessionWithWrongDate)));
  }

  @Test
  void read_a_specific_retake_exam_session_by_admin() throws ApiException {
    var retakeExam = apiAs(adminToken).getRetakeExamSessionById(futureM2Session.getId());

    assertNotNull(retakeExam);
    assertEquals(futureM2Session.getTitle(), retakeExam.getTitle());
  }

  @Test
  void filter_by_criteria_ok() {
    var pageable = PageRequest.of(0, 100);

    var resultAll = retakeExamSessionDao.filterByCriteria(null, null, pageable, null, null);
    assertTrue(
        resultAll.stream().map(RetakeExamSession::getId).toList().containsAll(ownedSessionIds));

    var resultTitle =
        retakeExamSessionDao.filterByCriteria(
            futureM2Session.getTitle(), null, pageable, null, null);
    assertEquals(1, resultTitle.size());
    assertEquals(futureM2Session.getTitle(), resultTitle.getFirst().getTitle());

    var from = Instant.now().plus(45 * 24, ChronoUnit.HOURS);
    var to = Instant.now().plus(100 * 24, ChronoUnit.HOURS);
    var resultDate = retakeExamSessionDao.filterByCriteria(null, null, pageable, from, to);

    assertTrue(
        resultDate.stream()
            .allMatch(
                s ->
                    s.getDateFrom().isAfter(Instant.from(from.minusSeconds(1)))
                        && s.getDateTo().isBefore(Instant.from(to.plusSeconds(1)))));
  }

  @Test
  void filter_retake_exam_session_by_student_level_ok() throws ApiException {
    var retakeExamSessionForM1 =
        apiAs(adminToken).getRetakeExamSessions(null, List.of(M1), null, null, 1, 100);

    assertNotNull(retakeExamSessionForM1);
    // only the M1/M2 session of this test carries M1
    assertTrue(idsOf(retakeExamSessionForM1).contains(futureM1M2Session.getId()));
    assertFalse(idsOf(retakeExamSessionForM1).contains(futureM2Session.getId()));
  }
}
