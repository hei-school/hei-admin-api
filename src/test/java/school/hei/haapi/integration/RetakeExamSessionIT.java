package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertBadRequestException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.test_data.RetakeExamSessionTestData.session1;
import static school.hei.haapi.integration.test_data.RetakeExamSessionTestData.session2;
import static school.hei.haapi.integration.test_data.RetakeExamSessionTestData.session3;
import static school.hei.haapi.integration.test_data.RetakeExamSessionTestData.sessionWithWrongDate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import school.hei.haapi.endpoint.rest.api.RetakeExamApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.RetakeExamSessionMapper;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.repository.RetakeExamSessionRepository;
import school.hei.haapi.repository.dao.RetakeExamSessionDao;

public class RetakeExamSessionIT extends FacadeITMockedThirdParties {
  @Autowired private RetakeExamSessionMapper retakeExamSessionMapper;
  @Autowired private RetakeExamSessionRepository retakeExamSessionRepository;
  @Autowired private RetakeExamSessionDao retakeExamSessionDao;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    retakeExamSessionRepository.saveAll(List.of(session1(), session2(), session3()));
  }

  @Test
  void read_all_retake_exam_sessions_by_student_ok() throws ApiException {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var retakeExamSessions = api.getRetakeExamSessions(null, null, null, null, null);

    assertNotNull(retakeExamSessions);
    assertEquals(3, retakeExamSessions.size());
  }

  @Test
  void read_all_retake_exam_sessions_by_admin_ok() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var retakeExamSessions = api.getRetakeExamSessions(null, null, null, null, null);

    assertNotNull(retakeExamSessions);
    assertEquals(3, retakeExamSessions.size());
  }

  @Test
  void filter_retake_exam_session_by_admin_ok() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var retakeExamFiltered = api.getRetakeExamSessions("session1", null, null, null, null);

    assertNotNull(retakeExamFiltered);
    assertEquals(1, retakeExamFiltered.size());
    assertEquals(session1().getId(), retakeExamFiltered.getFirst().getId());
    assertEquals(session1().getTitle(), retakeExamFiltered.getFirst().getTitle());
  }

  @Test
  void save_retake_exam_session_by_admin_ok() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var retakeExamSessionCreated =
        api.createOrUpdateRetakeExamSessions(retakeExamSessionMapper.toRest(session1()));

    assertNotNull(retakeExamSessionCreated);
    assertEquals(session1().getId(), retakeExamSessionCreated.getId());
    assertEquals(session1().getTitle(), retakeExamSessionCreated.getTitle());
  }

  @Test
  void save_retake_exam_session_with_wrong_date_ko() {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);
    var sessionWithWrongDate = sessionWithWrongDate();

    assertBadRequestException(
        "Session start date must be before end date",
        () ->
            api.createOrUpdateRetakeExamSessions(
                retakeExamSessionMapper.toRest(sessionWithWrongDate)));
  }

  @Test
  void read_a_specific_retake_exam_session_by_admin() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var retakeExam = api.getRetakeExamSessionById(session1().getId());

    assertNotNull(retakeExam);
    assertEquals(session1().getTitle(), retakeExam.getTitle());
  }

  @Test
  void filter_by_criteria_ok() {
    var session1 = session1();
    var session2 = session2();
    var session3 = session3();

    retakeExamSessionRepository.saveAll(List.of(session1, session2, session3));

    Pageable pageable = PageRequest.of(0, 10);

    var resultAll = retakeExamSessionDao.filterByCriteria(null, pageable, null, null);
    assertEquals(3, resultAll.size());

    var resultTitle = retakeExamSessionDao.filterByCriteria("session1", pageable, null, null);
    assertEquals(1, resultTitle.size());
    assertEquals("session1", resultTitle.getFirst().getTitle());

    Instant from = Instant.now().plus(45 * 24, ChronoUnit.HOURS);
    Instant to = Instant.now().plus(100 * 24, ChronoUnit.HOURS);
    var resultDate = retakeExamSessionDao.filterByCriteria(null, pageable, from, to);

    assertTrue(
        resultDate.stream()
            .allMatch(
                s ->
                    s.getDateFrom().isAfter(Instant.from(from.minusSeconds(1)))
                        && s.getDateTo().isBefore(Instant.from(to.plusSeconds(1)))));
  }
}
