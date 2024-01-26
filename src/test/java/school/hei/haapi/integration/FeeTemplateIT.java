package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.FEE_TEMPLATE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE_TEMPLATE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.createFeeTemplate2;
import static school.hei.haapi.integration.conf.TestUtils.feeTemplate1;
import static school.hei.haapi.integration.conf.TestUtils.feeTemplate2;
import static school.hei.haapi.integration.conf.TestUtils.feeTemplate3;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

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
import school.hei.haapi.endpoint.rest.model.FeeTemplate;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.file.BucketConf;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = FeeTemplateIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class FeeTemplateIT {

  @MockBean private SentryConf sentryConf;
  @MockBean private CognitoComponent cognitoComponentMock;
  @MockBean BucketConf bucketConf;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, FeeTemplateIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void manager_get_fee_templates() throws ApiException {
    ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(managerClient);

    List<FeeTemplate> actual = api.getFeeTemplates(null, null, null, 1, 10);

    assertEquals(2, actual.size());
    assertTrue(actual.contains(feeTemplate1()));
    assertTrue(actual.contains(feeTemplate3()));
  }

  @Test
  void manager_create_fee_templates() throws ApiException {
    ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(managerClient);

    FeeTemplate createdFeeTemplate =
        api.crupdateFeeTemplate(FEE_TEMPLATE2_ID, createFeeTemplate2());

    assertEquals(feeTemplate2().getName(), createdFeeTemplate.getName());
    assertEquals(feeTemplate2().getAmount(), createdFeeTemplate.getAmount());
    assertEquals(feeTemplate2().getNumberOfPayments(), createdFeeTemplate.getNumberOfPayments());
  }

  @Test
  void get_fee_template_by_id_existing() throws ApiException {
    ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(managerClient);

    FeeTemplate actual = api.getFeeTemplateById(FEE_TEMPLATE1_ID);

    assertEquals(feeTemplate1(), actual);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
