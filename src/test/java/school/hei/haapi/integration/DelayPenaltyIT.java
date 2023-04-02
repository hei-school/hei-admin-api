package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
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

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = DelayPenaltyIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class DelayPenaltyIT {
  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, DelayPenaltyIT.ContextInitializer.SERVER_PORT);
  }

  public static DelayPenalty delayPenalty1() {
    DelayPenalty delayPenalty = new DelayPenalty();
    delayPenalty.setId("delay_penalty1_id");
    delayPenalty.setInterestPercent(3);
    delayPenalty.setCreationDatetime(Instant.parse("2023-03-08T08:30:24Z"));
    delayPenalty.setInterestTimerate(DelayPenalty.InterestTimerateEnum.DAILY);
    delayPenalty.setGraceDelay(4);
    delayPenalty.setApplicabilityDelayAfterGrace(10);
    return delayPenalty;
  }

  public static CreateDelayPenaltyChange updateDelayPenalty() {
    return new CreateDelayPenaltyChange()
        .interestPercent(3)
        .interestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY)
        .graceDelay(4)
        .applicabilityDelayAfterGrace(10);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getDelayPenalty());
  }
  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createDelayPenaltyChange(updateDelayPenalty()));
  }
  @Test
  void teacher_read_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    PayingApi api = new PayingApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getDelayPenalty());
  }
  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    PayingApi api = new PayingApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createDelayPenaltyChange(updateDelayPenalty()));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    DelayPenalty actualDelayPenalty = api.getDelayPenalty();
    assertEquals(delayPenalty1(), actualDelayPenalty);
  }

  @Test
  void manager_write_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    DelayPenalty actualDelayPenalty = api.createDelayPenaltyChange(updateDelayPenalty());
    DelayPenalty expected = api.getDelayPenalty();
    assertEquals(expected, actualDelayPenalty);
  }

  @Test
  void manager_write_with_some_bad_fields_ko() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    CreateDelayPenaltyChange toUpdate1 = updateDelayPenalty().interestPercent(null);
    CreateDelayPenaltyChange toUpdate2 = updateDelayPenalty().graceDelay(null);
    CreateDelayPenaltyChange toUpdate3 = updateDelayPenalty().applicabilityDelayAfterGrace(null);
    CreateDelayPenaltyChange toUpdate4 = updateDelayPenalty().interestTimerate(null);
    CreateDelayPenaltyChange toUpdate5 = updateDelayPenalty().interestPercent(-1);
    CreateDelayPenaltyChange toUpdate6 = updateDelayPenalty().graceDelay(-1);
    CreateDelayPenaltyChange toUpdate7 = updateDelayPenalty().applicabilityDelayAfterGrace(-1);

    ApiException exception1 = assertThrows(ApiException.class,
        () -> api.createDelayPenaltyChange(toUpdate1));
    ApiException exception2 = assertThrows(ApiException.class,
        () -> api.createDelayPenaltyChange(toUpdate2));
    ApiException exception3 = assertThrows(ApiException.class,
        () -> api.createDelayPenaltyChange(toUpdate3));
    ApiException exception4 = assertThrows(ApiException.class,
        () -> api.createDelayPenaltyChange(toUpdate4));
    ApiException exception5 = assertThrows(ApiException.class,
        () -> api.createDelayPenaltyChange(toUpdate5));
    ApiException exception6 = assertThrows(ApiException.class,
        () -> api.createDelayPenaltyChange(toUpdate6));
    ApiException exception7 = assertThrows(ApiException.class,
        () -> api.createDelayPenaltyChange(toUpdate7));

    String exceptionMessage1 = exception1.getMessage();
    String exceptionMessage2 = exception2.getMessage();
    String exceptionMessage3 = exception3.getMessage();
    String exceptionMessage4 = exception4.getMessage();
    String exceptionMessage5 = exception5.getMessage();
    String exceptionMessage6 = exception6.getMessage();
    String exceptionMessage7 = exception7.getMessage();
    assertTrue(exceptionMessage1.contains("Interest Percent is mandatory"));
    assertTrue(exceptionMessage2.contains("GraceDelay is mandatory"));
    assertTrue(exceptionMessage3.contains("Applicability Delay after Grace is mandatory"));
    assertTrue(exceptionMessage4.contains("Interest Time rate is mandatory"));
    assertTrue(exceptionMessage5.contains("Interest Percent should be positive"));
    assertTrue(exceptionMessage6.contains("GraceDelay should be positive"));
    assertTrue(exceptionMessage7.contains("Applicability Delay after Grace should be positive"));
  }


  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
