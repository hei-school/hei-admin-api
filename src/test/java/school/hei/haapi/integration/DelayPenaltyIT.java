package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.FEE6_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = DelayPenaltyIT.ContextInitializer.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
            .graceDelay(10)
            .applicabilityDelayAfterGrace(3)
            .creationDatetime(Instant.parse("2022-11-15T08:25:25.00Z"));
  }

  static CreateDelayPenaltyChange createDelayPenaltyChange(){
    return new CreateDelayPenaltyChange()
            .interestPercent(1)
            .interestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY)
            .graceDelay(10)
            .applicabilityDelayAfterGrace(3);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  @Order(1)
  void student_read_delay_penalty_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    DelayPenalty actual = api.getDelayPenalty();

    assertEquals(delayPenalty(), actual);
  }

  @Test
  @Order(2)
  void manager_write_delay_penalty_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    DelayPenalty actual = api.createDelayPenaltyChange(createDelayPenaltyChange());
    actual.setId(null);
    actual.setCreationDatetime(null);
    DelayPenalty excepted = delayPenalty();
    excepted.setId(null);
    excepted.setInterestPercent(1);
    excepted.setCreationDatetime(null);

    assertEquals(excepted, actual);
  }

  @Test
  @Order(3)
  void student_fee_change_after_change_delay_penalty_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    Fee fee6 = api.getStudentFeeById(STUDENT1_ID,FEE6_ID);
    CreateDelayPenaltyChange createDelayPenaltyChange = createDelayPenaltyChange();
    createDelayPenaltyChange.setInterestPercent(5);
    DelayPenalty delayPenalty = api.createDelayPenaltyChange(createDelayPenaltyChange);

    Fee actualFee6 = api.getStudentFeeById(STUDENT1_ID,FEE6_ID);
    assertTrue(fee6.getTotalAmount() < actualFee6.getTotalAmount());
    assertEquals((5000+800),actualFee6.getTotalAmount());
  }

  @Test
  void student_write_ko() throws ApiException{
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsApiException(
            "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
            () -> api.createDelayPenaltyChange(createDelayPenaltyChange()));
  }

  @Test
  void teacher_write_ko() throws ApiException{
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    PayingApi api = new PayingApi(teacher1Client);

    assertThrowsApiException(
            "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
            () -> api.createDelayPenaltyChange(createDelayPenaltyChange()));
  }

  @Test
  void manager_write_with_some_bad_fields_ko() {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    CreateDelayPenaltyChange toCreate1 = createDelayPenaltyChange().graceDelay(-1);

    ApiException exception1 = assertThrows(ApiException.class,
            () -> api.createDelayPenaltyChange(toCreate1));

    String exceptionMessage1 = exception1.getMessage();
    assertTrue(exceptionMessage1.contains("Grace delay must be positive"));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
