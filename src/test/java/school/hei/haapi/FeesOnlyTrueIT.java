package school.hei.haapi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;

@Testcontainers
@AutoConfigureMockMvc
@TestPropertySource(properties = "FEES_ONLY=true")
class FeesOnlyEnabledIT extends FacadeITMockedThirdParties {

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void fees_only_endpoint_stays_accessible_when_fees_only_enabled() {
    var api = new PayingApi(anApiClient(MANAGER1_TOKEN));

    assertDoesNotThrow(() -> api.getFees(null, null, null, null, null, null, 1, 10, false, null));
  }

  @Test
  void non_fees_only_endpoint_is_blocked_when_fees_only_enabled() {
    var api = new UsersApi(anApiClient(MANAGER1_TOKEN));

    var exception =
        assertThrows(ApiException.class, () -> api.getMonitors(1, 10, null, null, null));

    assertEquals(403, exception.getCode());
  }
}
