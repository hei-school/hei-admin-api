package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE3_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

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
    List<Fee> AllPaidFeesWithoutInterest = api.getFees("PAID", 1, 20);

    DelayPenalty ActualDelayPenalty = api.getDelayPenalty();
    CreateDelayPenaltyChange actualCreateDelayPenalty = new CreateDelayPenaltyChange();
    actualCreateDelayPenalty.setInterestPercent(ActualDelayPenalty.getInterestPercent());
    actualCreateDelayPenalty.setInterestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY);
    actualCreateDelayPenalty.setGraceDelay(ActualDelayPenalty.getGraceDelay());
    actualCreateDelayPenalty.setApplicabilityDelayAfterGrace(ActualDelayPenalty.getApplicabilityDelayAfterGrace());
    api.createDelayPenaltyChange(actualCreateDelayPenalty);

    List<Fee> actualFeesWithInterest = api.getFees("", 1, 20);
    List<Fee> originalAllPaidFeesWithInterest = api.getFees("PAID", 1, 20);

    CreateDelayPenaltyChange newCreateDelayPenalty = new CreateDelayPenaltyChange();
    newCreateDelayPenalty.setInterestPercent(ActualDelayPenalty.getInterestPercent());
    newCreateDelayPenalty.setInterestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY);
    newCreateDelayPenalty.setGraceDelay(ActualDelayPenalty.getGraceDelay()+4);
    newCreateDelayPenalty.setApplicabilityDelayAfterGrace(ActualDelayPenalty.getApplicabilityDelayAfterGrace());
    api.createDelayPenaltyChange(newCreateDelayPenalty);

    List<Fee> updatedFeesWithInterest = api.getFees("", 1, 20);
    List<Fee> updatedAllPaidFees = api.getFees("PAID", 1, 20);

    //update a Delay Penalty automatically update all fees
    assertNotEquals(noUpdateFees,actualFeesWithInterest);

    Fee Fee7WithoutInterest = noUpdateFees.stream().filter(fee -> fee.getId().equals(fee7().getId())).collect(Collectors.toList()).get(0);
    Fee originalFee7WithInterest = actualFeesWithInterest.stream().filter(fee -> fee.getId().equals(fee7().getId())).collect(Collectors.toList()).get(0);
    Fee updatedFee7 = updatedFeesWithInterest.stream().filter(fee -> fee.getId().equals(fee7().getId())).collect(Collectors.toList()).get(0);
    //update grace day change automatically all fees with interest
    assertNotEquals(updatedFee7,originalFee7WithInterest);
    //have right total amount value + interest after change
    assertNotEquals(updatedFee7.getTotalAmount(),((Fee7WithoutInterest.getTotalAmount()*ActualDelayPenalty.getInterestPercent())*ActualDelayPenalty.getInterestPercent()*ActualDelayPenalty.getInterestPercent())*ActualDelayPenalty.getInterestPercent());

    //update grace day do not change all PAID fees with interest
    assertEquals(AllPaidFeesWithoutInterest,originalAllPaidFeesWithInterest);
    assertEquals(AllPaidFeesWithoutInterest,updatedAllPaidFees);

    Fee Fee9WithoutInterest = noUpdateFees.stream().filter(fee -> fee.getId().equals(fee9().getId())).collect(Collectors.toList()).get(0);
    Fee originalFee9WithInterest = actualFeesWithInterest.stream().filter(fee -> fee.getId().equals(fee9().getId())).collect(Collectors.toList()).get(0);
    Fee updatedFee9 = updatedFeesWithInterest.stream().filter(fee -> fee.getId().equals(fee9().getId())).collect(Collectors.toList()).get(0);
    //take off interest in fee when after update application of interest is before due day + grace day
    assertEquals(Fee9WithoutInterest.getTotalAmount(),updatedFee9.getTotalAmount());
    assertNotEquals(originalFee9WithInterest.getTotalAmount(),updatedFee9.getTotalAmount());

    Fee Fee3WithoutInterest = noUpdateFees.stream().filter(fee -> fee.getId().equals(fee3().getId())).collect(Collectors.toList()).get(0);
    Fee originalFee3WithInterest = actualFeesWithInterest.stream().filter(fee -> fee.getId().equals(fee3().getId())).collect(Collectors.toList()).get(0);
    Fee updatedFee3 = updatedFeesWithInterest.stream().filter(fee -> fee.getId().equals(fee3().getId())).collect(Collectors.toList()).get(0);
    //fee before application of delay penalty change no have interest
    assertEquals(Fee3WithoutInterest.getTotalAmount(),updatedFee3.getTotalAmount());
    assertEquals(originalFee3WithInterest.getTotalAmount(),updatedFee3.getTotalAmount());
  }



    @Test
    void changeInterestRateAutomaticChangeFees() throws ApiException {

        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PayingApi api = new PayingApi(manager1Client);

        List<Fee> noUpdateFees = api.getFees("", 1, 20);
        List<Fee> AllPaidFeesWithoutInterest = api.getFees("PAID", 1, 20);

        DelayPenalty ActualDelayPenalty = api.getDelayPenalty();
        CreateDelayPenaltyChange actualCreateDelayPenalty = new CreateDelayPenaltyChange();
        actualCreateDelayPenalty.setInterestPercent(ActualDelayPenalty.getInterestPercent());
        actualCreateDelayPenalty.setInterestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY);
        actualCreateDelayPenalty.setGraceDelay(ActualDelayPenalty.getGraceDelay());
        actualCreateDelayPenalty.setApplicabilityDelayAfterGrace(ActualDelayPenalty.getApplicabilityDelayAfterGrace());
        api.createDelayPenaltyChange(actualCreateDelayPenalty);

        List<Fee> actualFeesWithInterest = api.getFees("", 1, 20);
        List<Fee> originalAllPaidFeesWithInterest = api.getFees("PAID", 1, 20);

        Fee InitialFee7 = noUpdateFees.stream().filter(e->e.getId().equals("fee7_id")).collect(Collectors.toList()).get(0);
        Fee ActualFee7 = actualFeesWithInterest.stream().filter(e->e.getId().equals("fee7_id")).collect(Collectors.toList()).get(0);
        //Interest application for fee7 with application of 10 days divide in 02 parts, change of InterestRate
        //From 13-03-23 to 15-03-23 : InitialAmount = 5000; interestRate = 5; timeRate = DAILY
        int ApplyDays1 = 3;
        int InterestRate1 = 5;
        int expectedRemainingAmount = InterestFormulaDaily(InitialFee7.getRemainingAmount(),ApplyDays1,InterestRate1);
        //From 13-03-23 to 15-03-23 : interestRate = 4; timeRate = DAILY
        int ApplyDays2 = 7;
        int InterestRate2 = 4;
        expectedRemainingAmount = InterestFormulaDaily(expectedRemainingAmount,ApplyDays2,InterestRate2);

        assertEquals(expectedRemainingAmount,ActualFee7.getRemainingAmount());
    }

    int InterestFormulaDaily(int initial,int dayNumber,int rate){
        int Interest = 0;
        int amount = initial;
        for (int i = 0; i < dayNumber; i++) {
            Interest = amount*rate /100;
            amount = Interest+amount;
        }
        /*
        int amount = initial;
        for (int i = 0; i < dayNumber; i++) {
            amount = (int) Math.round(amount*(1+ ((double) rate /100)));
        }
         */
        return amount;
    }


  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
