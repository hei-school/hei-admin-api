package school.hei.haapi.integration;

import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;

public class StudentResultOverviewIT extends FacadeITMockedThirdParties {

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  public void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
  }

  @Test
  public void get_student_result_overviews_OK() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    UsersApi api = new UsersApi(apiClient);

    var studentsResultOverviews = api.getStudentsResultOverviewsByStatus(null, null, null, null);
  }
}
