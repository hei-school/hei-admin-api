package school.hei.haapi.integration;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.client.ApiException;

import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum.DAILY;
import static school.hei.haapi.integration.conf.TestUtils.*;
import static school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum.DAILY;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = DelayPenaltyIT.ContextInitializer.class)
@AutoConfigureMockMvc
@Slf4j
class DelayPenaltyIT {
  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, DelayPenaltyIT.ContextInitializer.SERVER_PORT);
  }

  public CreateDelayPenaltyChange toCrupdate(){
    return new CreateDelayPenaltyChange()
        .interestPercent(5)
        .graceDelay(10)
        .interestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY)
        .applicabilityDelayAfterGrace(5);
  }

  public static DelayPenalty delayPenalty2() {
    return new DelayPenalty()
        .id("delay_penalty1_id")
        .interestPercent(0)
        .interestTimerate(DAILY)
        .graceDelay(0)
        .applicabilityDelayAfterGrace(0);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    DelayPenalty actualDelayPenalty = api.getDelayPenalty().creationDatetime(null);

    assertEquals(actualDelayPenalty, delayPenalty2());
  }

  @Test
  void crupdate_delay_penalty_ok() throws ApiException{
    ApiClient manager1CLient = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1CLient);

    DelayPenalty actual = api.getDelayPenalty();
    DelayPenalty actualUpdated = api.createDelayPenaltyChange(toCrupdate());

    log.info("Actual: {}", actual);
    assertNotEquals(actual, actualUpdated);
  }

  @Test
  void crupdate_delay_penalty_should_update_fees() throws ApiException{
    ApiClient manager1CLient = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1CLient);

    List<Fee> lateFees = api.getFees("LATE", 1,10);
    for (Fee fee : lateFees) {
      log.info("now: {}", Instant.now().minus(10L, ChronoUnit.DAYS));
      log.info("applicability: {}",
          Instant.now().minus(10L, ChronoUnit.DAYS).plus(Duration.ofDays(10)).plus(Duration.ofDays(5)));
      fee.setDueDatetime(Instant.now().minus(10L, ChronoUnit.DAYS));
    }
    DelayPenalty actual = api.createDelayPenaltyChange(toCrupdate());
    List<Fee> updatedLateFees = api.getFees("LATE", 1 , 10);

    log.info("lateFees: {}", lateFees);
    log.info("actual: {}", actual);
    log.info("updatedLateFees: {}", updatedLateFees);
  }
  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
