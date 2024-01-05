package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.FeeType;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = FeeTypeIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class FeeTypeIT {

  @MockBean private SentryConf sentryConf;
  @MockBean private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, FeeTypeIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void manager_get_predefined_types() throws ApiException {
    ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(managerClient);
    List<FeeType> actual = api.getFeeTypes(null, 200000, 9, 1, 10);
    assertEquals(1, actual.size());
    assertTrue(actual.contains(feeType1()));
  }

  @Test
  void manager_create_fee_types() throws ApiException {
    ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(managerClient);
    FeeType createdFeeType = api.crupdateFeeType(FEE_TYPE2_ID, createFeeType2());
    assertEquals(feetype2().getName(), createdFeeType.getName());
    assertEquals(feetype2().getTotalAmount(), createdFeeType.getTotalAmount());
    assertEquals(feetype2().getNumberOfMonths(), createdFeeType.getNumberOfMonths());
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
