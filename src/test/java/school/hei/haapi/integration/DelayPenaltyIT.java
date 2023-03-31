package school.hei.haapi.integration;

import java.time.Instant;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.InterestTimerate.DAILY;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
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

  static DelayPenalty delayPenalty2() {
    return new DelayPenalty().id("delay_penalty2_id").interestPercent(2).interestTimerate(DAILY)
        .graceDelay(16).applicabilityDelayAfterGrace(2)
        .creationDatetime(Instant.parse("2022-11-10T08:25:25Z"));
  }

  DelayPenalty createdDelayPenalty() {
    return new DelayPenalty()
        .graceDelay(3)
        .interestPercent(5)
        .applicabilityDelayAfterGrace(2)
        .interestTimerate(DAILY);
  }

  CreateDelayPenaltyChange toCreate() {
    return new CreateDelayPenaltyChange()
        .graceDelay(3)
        .interestPercent(5)
        .applicabilityDelayAfterGrace(2)
        .interestTimerate(DAILY);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void read_latest_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    DelayPenalty actual = api.getDelayPenalty();

    assertEquals(delayPenalty2(), actual);
  }

  @Test
  void delay_penalty_change_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    DelayPenalty actual = api.createDelayPenaltyChange(toCreate());
    assertEquals(
        createdDelayPenalty()
            .id(actual.getId())
            .creationDatetime(actual.getCreationDatetime()),
        actual);
  }

  @Test
  void delay_penalty_change_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsApiException("{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createDelayPenaltyChange(toCreate()));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
