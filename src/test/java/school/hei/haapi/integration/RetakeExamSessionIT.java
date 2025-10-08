package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertBadRequestException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.test_data.RetakeExamSessionTestData.session1;
import static school.hei.haapi.integration.test_data.RetakeExamSessionTestData.sessionWithWrongDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.api.RetakeExamApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.RetakeExamSessionMapper;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;

public class RetakeExamSessionIT extends FacadeITMockedThirdParties {
  @Autowired private RetakeExamSessionMapper retakeExamSessionMapper;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
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
    assertEquals(retakeExamSessionMapper.toRest(session1()), retakeExamFiltered.getFirst());
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

    var retakeExamSession = api.getRetakeExamSessionById(session1().getId());

    assertNotNull(retakeExamSession);
    assertEquals(retakeExamSessionMapper.toRest(session1()), retakeExamSession);
  }
}
