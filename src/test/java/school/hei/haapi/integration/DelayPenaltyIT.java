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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange.InterestTimerateEnum;
import static school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum.DAILY;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
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
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  static DelayPenalty default_delayPenalty() {
    return new DelayPenalty()
        .graceDelay(2)
        .applicabilityDelayAfterGrace(5)
        .interestPercent(2)
        .interestTimerate(DAILY);
  }

  static DelayPenalty expected_createDelayPenaltyChange() {
    CreateDelayPenaltyChange toBeCreated = createDelayPenaltyChange();
    return default_delayPenalty()
        .graceDelay(toBeCreated.getGraceDelay())
        .interestPercent(toBeCreated.getInterestPercent())
        .applicabilityDelayAfterGrace(toBeCreated.getApplicabilityDelayAfterGrace());
  }

  static CreateDelayPenaltyChange createDelayPenaltyChange() {
    return new CreateDelayPenaltyChange()
        .interestTimerate(InterestTimerateEnum.DAILY)
        .graceDelay(10)
        .applicabilityDelayAfterGrace(10)
        .interestPercent(10);
  }

  static CreateDelayPenaltyChange createDelayPenaltyChange1() {
    return new CreateDelayPenaltyChange()
        .graceDelay(default_delayPenalty().getGraceDelay())
        .interestPercent(default_delayPenalty().getInterestPercent())
        .applicabilityDelayAfterGrace(default_delayPenalty().getApplicabilityDelayAfterGrace())
        .interestTimerate(InterestTimerateEnum.DAILY);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void get_delay_penalty_configuration_ok() {
    PayingApi managerApi = new PayingApi(anApiClient(MANAGER1_TOKEN));
    PayingApi studentApi = new PayingApi(anApiClient(STUDENT1_TOKEN));
    PayingApi teacherApi = new PayingApi(anApiClient(TEACHER1_TOKEN));

    assertThrowsApiException(
        "{\"type\":\"404 NOT_FOUND\",\"message\":\"Delay penalty not found\"}",
        managerApi::getDelayPenalty);
    assertThrowsApiException(
        "{\"type\":\"404 NOT_FOUND\",\"message\":\"Delay penalty not found\"}",
        studentApi::getDelayPenalty);
    assertThrowsForbiddenException(
        () -> teacherApi.createDelayPenaltyChange(createDelayPenaltyChange1()));
  }

  @Test
  void manager_get_delay_penalty_ok() throws ApiException {
    ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(managerClient);

    DelayPenalty expected = expected_createDelayPenaltyChange();
    DelayPenalty actual = api.createDelayPenaltyChange(createDelayPenaltyChange1());

    assertEquals(expected.getGraceDelay(), actual.getGraceDelay());
    assertEquals(expected.getInterestPercent(), actual.getInterestPercent());
    assertEquals(expected.getApplicabilityDelayAfterGrace(),
        actual.getApplicabilityDelayAfterGrace());
    assertEquals(expected.getInterestTimerate(), actual.getInterestTimerate());
  }

  @Test
  void manager_update_current_delay_penalty_configuration_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    DelayPenalty expected = expected_createDelayPenaltyChange();
    DelayPenalty actual = api.createDelayPenaltyChange(createDelayPenaltyChange());
    DelayPenalty actual1 = api.createDelayPenaltyChange(createDelayPenaltyChange());

    assertEquals(actual.getId(), actual1.getId());

    assertEquals(expected.getGraceDelay(), actual.getGraceDelay());
    assertEquals(expected.getInterestPercent(), actual.getInterestPercent());
    assertEquals(expected.getApplicabilityDelayAfterGrace(),
        actual.getApplicabilityDelayAfterGrace());
    assertEquals(expected.getInterestTimerate(), actual.getInterestTimerate());
  }

  @Test
  void teacher_update_current_delay_penalty_configuration_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    PayingApi api = new PayingApi(teacher1Client);

    assertThrowsForbiddenException(() -> api.createDelayPenaltyChange(createDelayPenaltyChange()));
  }

  @Test
  void student_update_current_delay_penalty_configuration_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsForbiddenException(() -> api.createDelayPenaltyChange(createDelayPenaltyChange()));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
