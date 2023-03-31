package school.hei.haapi.integration;

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
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = DelayPenaltyIT.ContextInitializer.class)
@AutoConfigureMockMvc
class DelayPenaltyIT {
  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, DelayPenaltyIT.ContextInitializer.SERVER_PORT);
  }

  static DelayPenalty delayPenalty() {
    return new DelayPenalty()
            .id("delay_penalty_id")
            .interestPercent(2)
            .interestTimerate(DelayPenalty.InterestTimerateEnum.DAILY)
            .graceDelay(3)
            .applicabilityDelayAfterGrace(10)
            .creationDatetime(Instant.parse("2022-11-15T08:25:25.00Z"));
  }

  static CreateDelayPenaltyChange createDelayPenaltyChange(){
    return new CreateDelayPenaltyChange()
            .interestPercent(1)
            .interestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY)
            .graceDelay(3)
            .applicabilityDelayAfterGrace(10);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void student_read_delay_penalty_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    DelayPenalty actual = api.getDelayPenalty();

    assertEquals(delayPenalty(), actual);
  }

  @Test
  void manager_write_delay_penalty_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    DelayPenalty actual = api.createDelayPenaltyChange(createDelayPenaltyChange());
    actual.setCreationDatetime(null);
    DelayPenalty expected = delayPenalty();
    expected.setId(null);
    expected.setInterestPercent(1);
    expected.setCreationDatetime(null);

    assertEquals(expected, actual);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
