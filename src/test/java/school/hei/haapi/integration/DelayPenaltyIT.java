package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE3_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
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

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = DelayPenaltyIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class DelayPenaltyIT {
  private static DelayPenaltyHistoryService delayPenaltyHistoryService;
  private static InterestHistoryService interestHistoryService;
  private static FeeService feeService;
  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, DelayPenaltyIT.ContextInitializer.SERVER_PORT);
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
        .interestPercent(1)
        .interestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY)
        .graceDelay(1)
        .applicabilityDelayAfterGrace(10);
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

  @BeforeTestMethod
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    DelayPenalty actualDelayPenalty = api.getDelayPenalty();
    assertEquals(delayPenalty1(), actualDelayPenalty);
  }

  @Test
  void student_paid_fee() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    Fee student1Paid = api.getStudentFeeById(STUDENT1_ID, "fee1_id");
    assertEquals(5000, student1Paid.getTotalAmount());
  }

  @Test
  void fee_applied_penalty() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    Fee student1Late = api.getStudentFeeById(STUDENT1_ID, "fee8_id");
    assertTrue(student1Late.getTotalAmount() > 5000);
  }

  @Test
  void create_unpaid_fee_interest_not_applied() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    List<Fee> createNewFee = api.createStudentFees(STUDENT1_ID, List.of(creatableFee1()));
    assertEquals(Fee.StatusEnum.UNPAID, createNewFee.get(0).getStatus());
    assertEquals(5000, createNewFee.get(0).getTotalAmount());
  }

  @Test
  void manager_write_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    DelayPenalty actualDelayPenalty = api.createDelayPenaltyChange(updateDelayPenalty());
    DelayPenalty expected = api.getDelayPenalty();
    assertEquals(expected, actualDelayPenalty);
    //todo: add creation delayPenaltyHistory test
  }

  @Test
  void manager_write_with_some_bad_fields_ko() {
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
  @Test
  void changeDelayPenaltyNotChangePaidFees() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    List<Fee> beforeUpdateFees = api.getFees(String.valueOf(Fee.StatusEnum.PAID), 1, 10);
    api.createDelayPenaltyChange(updateDelayPenalty());
    List<Fee> afterUpdateFees = api.getFees(String.valueOf(Fee.StatusEnum.PAID), 1, 10);
    assertEquals(beforeUpdateFees,afterUpdateFees);
  }
  @Test
  void changeDelayPenaltyChangeNotPaidFees() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    List<Fee> lateFeeBeforeUpdateFees = api.getFees(String.valueOf(Fee.StatusEnum.LATE), 1, 10);
    List<Fee> unpaidFeeBeforeUpdateFees = api.getFees(String.valueOf(Fee.StatusEnum.UNPAID), 1, 10);
    api.createDelayPenaltyChange(updateDelayPenalty());
    List<Fee> lateFeeAfterUpdateFees = api.getFees(String.valueOf(Fee.StatusEnum.LATE), 1, 10);
    List<Fee> unpaidFeeAfterUpdateFees = api.getFees(String.valueOf(Fee.StatusEnum.UNPAID), 1, 10);
    Logger.getAnonymousLogger(lateFeeBeforeUpdateFees.toString());
    assertNotEquals(lateFeeBeforeUpdateFees,lateFeeAfterUpdateFees);
    assertNotEquals(unpaidFeeBeforeUpdateFees,unpaidFeeAfterUpdateFees);
  }
  @Test
  void increaseGraceDelayInDelayPenaltyChangeAmount() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    Fee beforeUpdateFee = api.getStudentFeeById(fee7().getStudentId(),fee7().getId());

    DelayPenalty ActualDelayPenalty = api.getDelayPenalty();
    CreateDelayPenaltyChange newDelayPenalty = new CreateDelayPenaltyChange();
    newDelayPenalty.setInterestPercent(ActualDelayPenalty.getInterestPercent());
    newDelayPenalty.setInterestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY);
    newDelayPenalty.setGraceDelay(ActualDelayPenalty.getGraceDelay());
    newDelayPenalty.setApplicabilityDelayAfterGrace(ActualDelayPenalty.getApplicabilityDelayAfterGrace());

    api.createDelayPenaltyChange(updateDelayPenalty());

    Fee afterUpdateFee = api.getStudentFeeById(fee7().getStudentId(),fee7().getId());
    assertEquals(beforeUpdateFee,afterUpdateFee);
  }

  @Test
  void decreaseGraceDelayInDelayPenaltyChangeAmount() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    DelayPenalty ActualDelayPenalty = api.getDelayPenalty();

    CreateDelayPenaltyChange newDelayPenalty = new CreateDelayPenaltyChange();
    newDelayPenalty.setInterestPercent(ActualDelayPenalty.getInterestPercent());
    newDelayPenalty.setInterestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY);
    newDelayPenalty.setGraceDelay(ActualDelayPenalty.getGraceDelay());
    newDelayPenalty.setApplicabilityDelayAfterGrace(ActualDelayPenalty.getApplicabilityDelayAfterGrace());

    api.createDelayPenaltyChange(updateDelayPenalty());
  }

  @Test
  void increaseApplicabilityDelayInDelayPenaltyChangeAmount() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    DelayPenalty ActualDelayPenalty = api.getDelayPenalty();

    CreateDelayPenaltyChange newDelayPenalty = new CreateDelayPenaltyChange();
    newDelayPenalty.setInterestPercent(ActualDelayPenalty.getInterestPercent());
    newDelayPenalty.setInterestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY);
    newDelayPenalty.setGraceDelay(ActualDelayPenalty.getGraceDelay());
    newDelayPenalty.setApplicabilityDelayAfterGrace(ActualDelayPenalty.getApplicabilityDelayAfterGrace());

    api.createDelayPenaltyChange(updateDelayPenalty());


  }

  @Test
  void decreaseApplicabilityDelayInDelayPenaltyChangeAmount() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    DelayPenalty ActualDelayPenalty = api.getDelayPenalty();

    CreateDelayPenaltyChange newDelayPenalty = new CreateDelayPenaltyChange();
    newDelayPenalty.setInterestPercent(ActualDelayPenalty.getInterestPercent());
    newDelayPenalty.setInterestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY);
    newDelayPenalty.setGraceDelay(ActualDelayPenalty.getGraceDelay());
    newDelayPenalty.setApplicabilityDelayAfterGrace(ActualDelayPenalty.getApplicabilityDelayAfterGrace());

    api.createDelayPenaltyChange(updateDelayPenalty());
  }

  @Test
  void increaseInterestPercentInDelayPenaltyChangeAmount() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    DelayPenalty ActualDelayPenalty = api.getDelayPenalty();

    CreateDelayPenaltyChange newDelayPenalty = new CreateDelayPenaltyChange();
    newDelayPenalty.setInterestPercent(ActualDelayPenalty.getInterestPercent());
    newDelayPenalty.setInterestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY);
    newDelayPenalty.setGraceDelay(ActualDelayPenalty.getGraceDelay());
    newDelayPenalty.setApplicabilityDelayAfterGrace(ActualDelayPenalty.getApplicabilityDelayAfterGrace());

    api.createDelayPenaltyChange(updateDelayPenalty());;
  }

  @Test
  void decreaseInterestPercentInDelayPenaltyChangeAmount() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    DelayPenalty ActualDelayPenalty = api.getDelayPenalty();

    CreateDelayPenaltyChange newDelayPenalty = new CreateDelayPenaltyChange();
    newDelayPenalty.setInterestPercent(ActualDelayPenalty.getInterestPercent());
    newDelayPenalty.setInterestTimerate(CreateDelayPenaltyChange.InterestTimerateEnum.DAILY);
    newDelayPenalty.setGraceDelay(ActualDelayPenalty.getGraceDelay());
    newDelayPenalty.setApplicabilityDelayAfterGrace(ActualDelayPenalty.getApplicabilityDelayAfterGrace());

    api.createDelayPenaltyChange(updateDelayPenalty());
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
