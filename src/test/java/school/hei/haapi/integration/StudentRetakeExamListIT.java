package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.api.StudentsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CourseResultStatus;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;

public class StudentRetakeExamListIT extends FacadeITMockedThirdParties {
  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
  }

  @Test
  void should_be_see_all_exams_VALIDATED() throws ApiException {
    var api = new StudentsApi(anApiClient(MANAGER1_TOKEN));
    var status = CourseResultStatus.VALIDATED;
    var results = api.getListStudentRetakeExams(STUDENT1_ID, status).toArray().length;
    var expected = 1;

    assertEquals(expected, results);
  }

  @Test
  void should_be_see_my_all_exams_INCOMPLETED() throws ApiException {
    var api = new StudentsApi(anApiClient(STUDENT1_TOKEN));
    var status = CourseResultStatus.INCOMPLETE;
    var results = api.getListStudentRetakeExams(STUDENT1_ID, status);

    assertNotNull(results, "Results should be not null");
  }

  @Test
  void should_be_see_all_exams_with_all_status() throws ApiException {
    var api = new StudentsApi(anApiClient(MANAGER1_TOKEN));
    var results = api.getListStudentRetakeExams(STUDENT1_ID, null).toArray().length;
    var expected = 3;
    assertEquals(expected, results);
  }
}
