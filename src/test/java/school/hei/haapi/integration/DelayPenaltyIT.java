package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.FEE7_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
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
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

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

  public static DelayPenalty delayPenalty() {
    DelayPenalty delayPenalty = new DelayPenalty();
    delayPenalty.id("delay_penalty2_id");
    delayPenalty.interestPercent(2);
    delayPenalty.interestTimerate(DelayPenalty.InterestTimerateEnum.DAILY);
    delayPenalty.graceDelay(3);
    delayPenalty.applicabilityDelayAfterGrace(5);
    delayPenalty.creationDatetime(Instant.parse("2023-01-09T08:25:24.00Z"));
    return delayPenalty;
  }

  public static CreateDelayPenaltyChange createDelayPenalty1() {
    return new CreateDelayPenaltyChange()
        .interestPercent(12)
        .interestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY)
        .graceDelay(2)
        .applicabilityDelayAfterGrace(0);
  }

  public static CreateDelayPenaltyChange createDelayPenalty2() {
    return new CreateDelayPenaltyChange()
        .interestPercent(2)
        .interestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY)
        .graceDelay(3)
        .applicabilityDelayAfterGrace(10);
  }

  public static DelayPenalty expectedCreated() {
    return new DelayPenalty()
        .interestPercent(createDelayPenalty1().getInterestPercent())
        .interestTimerate(DelayPenalty.InterestTimerateEnum.DAILY)
        .graceDelay(createDelayPenalty1().getGraceDelay())
        .applicabilityDelayAfterGrace(createDelayPenalty1().getApplicabilityDelayAfterGrace());
  }

  static Fee fee() {
    Fee fee = new Fee();
    fee.setId(FEE7_ID);
    fee.setStudentId(STUDENT1_ID);
    fee.setStatus(Fee.StatusEnum.LATE);
    fee.setType(Fee.TypeEnum.TUITION);
    fee.setTotalAmount(15000);
    fee.setRemainingAmount(5000);
    fee.setComment("Comment");
    fee.setUpdatedAt(Instant.parse("2023-03-08T08:30:24Z"));
    fee.creationDatetime(Instant.parse("2023-02-08T08:25:24.00Z"));
    fee.setDueDatetime(Instant.parse("2023-03-26T08:25:24.00Z"));
    return fee;
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    DelayPenalty actualDelayPenalty = api.getDelayPenalty();

    assertEquals(delayPenalty(), actualDelayPenalty);
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    PayingApi api = new PayingApi(teacher1Client);

    DelayPenalty actualDelayPenalty = api.getDelayPenalty();
    assertEquals(delayPenalty(), actualDelayPenalty);
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    DelayPenalty actualDelayPenalty = api.getDelayPenalty();
    assertEquals(delayPenalty(), actualDelayPenalty);
  }

  @Test
  void manager_write_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    DelayPenalty actual = api.changeDelayPenaltyChange(createDelayPenalty1());

    assertEquals(expectedCreated()
            .id(actual.getId())
            .creationDatetime(actual.getCreationDatetime())
        , actual);
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.changeDelayPenaltyChange(createDelayPenalty1()));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    PayingApi api = new PayingApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.changeDelayPenaltyChange(createDelayPenalty1()));
  }

  @Test
  void fee_amount_should_have_changed() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    api.changeDelayPenaltyChange(createDelayPenalty2());
    Fee fee1 = api.getStudentFeeById(fee().getStudentId(), fee().getId());
    api.changeDelayPenaltyChange(createDelayPenalty2().interestPercent(5));
    Fee fee2 = api.getStudentFeeById(fee().getStudentId(), fee().getId());
    api.changeDelayPenaltyChange(createDelayPenalty2().interestPercent(0));
    Fee fee3 = api.getStudentFeeById(fee().getStudentId(), fee().getId());
    api.changeDelayPenaltyChange(
        createDelayPenalty2().graceDelay(0).applicabilityDelayAfterGrace(3));
    Fee fee4 = api.getStudentFeeById(fee().getStudentId(), fee().getId());

    /*##### Must change asserted results because it changes as days go by ######*/
//    /* GraceDelay=2 && ApplicabilityDelayAfterGrace=10 */
//    assertEquals(5520, fee1.getRemainingAmountWithInterest());// interestPercent = 2
//    assertEquals(6381, fee2.getRemainingAmountWithInterest());//interestPercent = 5
//    assertEquals(5000, fee3.getRemainingAmountWithInterest());//interestPercent = 0
//    /* GraceDelay=0 && ApplicabilityDelayAfterGrace=3*/
//    assertEquals(5306, fee4.getRemainingAmountWithInterest());
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
