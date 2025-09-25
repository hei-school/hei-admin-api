package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.test_data.RetakeExamSessionTestData.session1;

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
    setUpS3Service(fileService, student1());
  }

  @Test
  void read_all_retake_exam_sessions_by_student_ok() throws ApiException {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var retakeAxamSessions = api.getRetakeExamSessions(null, null, null, null, null);

    assertNotNull(retakeAxamSessions);
    assertEquals(3, retakeAxamSessions.size());
  }

  @Test
  void read_all_retake_exam_sessions_by_admin_ok() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var retakeAxamSessions = api.getRetakeExamSessions(null, null, null, null, null);

    assertNotNull(retakeAxamSessions);
    assertEquals(3, retakeAxamSessions.size());
  }

  @Test
  void save_retake_exam_session_by_admin() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    RetakeExamApi api = new RetakeExamApi(apiClient);

    var retakeExamSessionCreated =
        api.createOrUpdateRetakeExamSessions(retakeExamSessionMapper.toRest(session1()));

    assertNotNull(retakeExamSessionCreated);
    assertEquals(session1().getTitle(), retakeExamSessionCreated.getTitle());
  }
}
