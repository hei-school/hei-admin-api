package school.hei.haapi.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.service.DelayPenaltyHistoryService;
import school.hei.haapi.service.FeeService;
import school.hei.haapi.service.InterestHistoryService;
import school.hei.haapi.service.utils.DataFormatterUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = DelayPenaltyChangeIT.ContextInitializer.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class DelayPenaltyChangeIT {
  private static DelayPenaltyHistoryService delayPenaltyHistoryService;
  private static InterestHistoryService interestHistoryService;
  private static FeeService feeService;
  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, DelayPenaltyChangeIT.ContextInitializer.SERVER_PORT);
  }

  static CreateFee creatableFee1() {
    return new CreateFee()
        .type(CreateFee.TypeEnum.TUITION)
        .totalAmount(5000)
        .comment("Comment")
        .dueDatetime(Instant.parse("2023-10-08T08:25:24.00Z"));
  }
  public static DelayPenalty delayPenalty1() {
    DelayPenalty delayPenalty = new DelayPenalty();
    delayPenalty.setId("delay_penalty1_id");
    delayPenalty.setInterestPercent(updateDelayPenalty().getInterestPercent());
    delayPenalty.setCreationDatetime(Instant.parse("2023-03-08T08:30:24Z"));
    delayPenalty.setInterestTimerate(DelayPenalty.InterestTimerateEnum.DAILY);
    delayPenalty.setGraceDelay(updateDelayPenalty().getGraceDelay());
    delayPenalty.setApplicabilityDelayAfterGrace(updateDelayPenalty().getApplicabilityDelayAfterGrace());
    return delayPenalty;
  }

  public static CreateDelayPenaltyChange updateDelayPenalty() {
    return new CreateDelayPenaltyChange()
        .interestPercent(5)
        .interestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY)
        .graceDelay(2)
        .applicabilityDelayAfterGrace(20);
  }

  static Fee fee1() {
    Fee fee = new Fee();
    fee.setId(FEE1_ID);
    fee.setStudentId(STUDENT1_ID);
    fee.setStatus(Fee.StatusEnum.PAID);
    fee.setType(Fee.TypeEnum.TUITION);
    fee.setTotalAmount(5000);
    fee.setRemainingAmount(0);
    fee.setComment("Comment");
    fee.setUpdatedAt(Instant.parse("2023-02-08T08:30:24Z"));
    fee.creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    fee.setDueDatetime(Instant.parse("2021-12-08T08:25:24.00Z"));
    return fee;
  }

  static Fee fee2() {
    Fee fee = new Fee();
    fee.setId(FEE2_ID);
    fee.setStudentId(STUDENT1_ID);
    fee.setStatus(Fee.StatusEnum.PAID);
    fee.setType(Fee.TypeEnum.HARDWARE);
    fee.setTotalAmount(5000);
    fee.setRemainingAmount(0);
    fee.setComment("Comment");
    fee.setUpdatedAt(Instant.parse("2023-02-08T08:30:24Z"));
    fee.creationDatetime(Instant.parse("2021-11-10T08:25:24.00Z"));
    fee.setDueDatetime(Instant.parse("2021-12-10T08:25:24.00Z"));
    return fee;
  }

  static Fee fee3() {
    Fee fee = new Fee();
    fee.setId(FEE3_ID);
    fee.setStudentId(STUDENT1_ID);
    fee.setStatus(Fee.StatusEnum.LATE);
    fee.setType(Fee.TypeEnum.TUITION);
    fee.setTotalAmount(5000);
    fee.setRemainingAmount(5000);
    fee.setComment("Comment");
    fee.setUpdatedAt(Instant.parse("2023-02-08T08:30:24Z"));
    fee.creationDatetime(Instant.parse("2022-12-08T08:25:24.00Z"));
    fee.setDueDatetime(Instant.parse("2021-12-09T08:25:24.00Z"));
    return fee;
  }
  static Fee fee7() {
    Fee fee = new Fee();
    fee.setId("fee7_id");
    fee.setStudentId(STUDENT1_ID);
    fee.setStatus(Fee.StatusEnum.LATE);
    fee.setType(Fee.TypeEnum.TUITION);
    fee.setTotalAmount(5000);
    fee.setRemainingAmount(5000);
    fee.setComment("Comment");
    fee.setUpdatedAt(Instant.parse("2023-02-08T08:30:24.00Z"));
    fee.creationDatetime(Instant.parse("2021-11-11T08:25:24.00Z"));
    fee.setDueDatetime(Instant.parse("2023-03-08T08:25:24.00Z"));
    return fee;
  }
  static Fee fee8() {
    Fee fee = new Fee();
    fee.setId("fee8_id");
    fee.setStudentId(STUDENT1_ID);
    fee.setStatus(Fee.StatusEnum.LATE);
    fee.setType(Fee.TypeEnum.TUITION);
    fee.setTotalAmount(5000);
    fee.setRemainingAmount(5000);
    fee.setComment("Comment");
    fee.setUpdatedAt(Instant.parse("2023-02-08T08:30:24.00Z"));
    fee.creationDatetime(Instant.parse("2021-11-12T08:25:24.00Z"));
    fee.setDueDatetime(Instant.parse("2023-03-14T08:25:24.00Z"));
    return fee;
  }
  static Fee fee9() {
    Fee fee = new Fee();
    fee.setId("fee9_id");
    fee.setStudentId(STUDENT1_ID);
    fee.setStatus(Fee.StatusEnum.LATE);
    fee.setType(Fee.TypeEnum.TUITION);
    fee.setTotalAmount(5000);
    fee.setRemainingAmount(1000);
    fee.setComment("Comment");
    fee.setUpdatedAt(Instant.parse("2023-02-08T08:30:24.00Z"));
    fee.creationDatetime(Instant.parse("2022-12-13T08:25:24.00Z"));
    fee.setDueDatetime(Instant.parse("2023-12-20T08:25:24.00Z"));
    return fee;
  }
  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
   void changeGraceDelayAutomaticChangeFees() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    List<Fee> noUpdateFees = api.getFees("", 1, 20);

    DelayPenalty ActualDelayPenalty = api.getDelayPenalty();
    CreateDelayPenaltyChange actualCreateDelayPenalty = new CreateDelayPenaltyChange();
    actualCreateDelayPenalty.setInterestPercent(ActualDelayPenalty.getInterestPercent());
    actualCreateDelayPenalty.setInterestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY);
    actualCreateDelayPenalty.setGraceDelay(ActualDelayPenalty.getGraceDelay());
    actualCreateDelayPenalty.setApplicabilityDelayAfterGrace(ActualDelayPenalty.getApplicabilityDelayAfterGrace());
    api.createDelayPenaltyChange(actualCreateDelayPenalty);

    List<Fee> actualFeesWithInterest = api.getFees("", 1, 20);

    CreateDelayPenaltyChange newCreateDelayPenalty = new CreateDelayPenaltyChange();
    actualCreateDelayPenalty.setInterestPercent(ActualDelayPenalty.getInterestPercent());
    actualCreateDelayPenalty.setInterestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY);
    actualCreateDelayPenalty.setGraceDelay(ActualDelayPenalty.getGraceDelay()+5);
    actualCreateDelayPenalty.setApplicabilityDelayAfterGrace(ActualDelayPenalty.getApplicabilityDelayAfterGrace());
    api.createDelayPenaltyChange(newCreateDelayPenalty);

    List<Fee> updatedFeesWithInterest = api.getFees("", 1, 20);


    assertEquals(noUpdateFees,actualFeesWithInterest);
    assertEquals(actualFeesWithInterest,updatedFeesWithInterest);
  }


  static Instant mockTimeToInstant(String time){
    Clock clock = Clock.fixed(Instant.parse(time), ZoneId.of("UTC"));
    return Instant.now(clock);
  }


  @Test
  public void manager_read_ok_time_mock() {
    String instantExpected = "2014-12-22T10:15:30Z";
    Clock clock = Clock.fixed(Instant.parse(instantExpected), ZoneId.of("UTC"));
    Instant instant = Instant.now(clock);
    Instant due = mockTimeToInstant("2021-12-09T08:25:24.00Z");

    // try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
    try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
      LocalDate ok = DataFormatterUtils.takeLocalDate();

      mockedStatic.when(Instant::now).thenReturn(due);
      LocalDate expected = LocalDate.ofInstant(Instant.now(),ZoneId.of("UTC")).plusDays(1);
      LocalDate actual = DataFormatterUtils.takeLocalDate();

      mockedStatic.when(Instant::now).thenReturn(due);

      assertEquals(expected,actual);
    }
  }


  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
