package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.event.gen.CheckMobilePaymentTransactionTriggered;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.model.MpbsVerification;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.repository.dao.MpbsDao;
import school.hei.haapi.service.event.CheckMobilePaymentTransactionTriggeredService;
import school.hei.haapi.service.mobileMoney.MobileMoneyApi;
import school.hei.haapi.service.mobileMoney.MobileMoneyApiFacade;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.AIRTEL_MONEY;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.MVOLA;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.integration.MpbsIT.expectedMpbs1Domain;
import static school.hei.haapi.integration.MpbsVerificationIT.ContextInitializer.SERVER_PORT;
import static school.hei.haapi.integration.StudentIT.mapRestStudentToDomain;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.domainFee1;
import static school.hei.haapi.integration.conf.TestUtils.fee1;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MpbsVerificationIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class MpbsVerificationIT extends MockedThirdParties {
  @Autowired private CheckMobilePaymentTransactionTriggeredService subject;
  @MockBean
  EventBridgeClient eventBridgeClient;
  @MockBean private MpbsDao mpbsDaoMock;
  @MockBean(name = "OrangeApi")
  MobileMoneyApi orangeApiMock;
  @MockBean(name = "MvolaApi")
  MobileMoneyApi mvolaApiMock;


  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
    setUpMpbsDao(mpbsDaoMock);
    setUpMobileMock(orangeApiMock);
    setUpMobileMock(mvolaApiMock);
  }

  private void setUpMobileMock(MobileMoneyApi mobileMoneyApi) {
    when(mobileMoneyApi.getByTransactionRef(eq(MVOLA), any())).thenReturn(attemptTransactionTelma());
    when(mobileMoneyApi.getByTransactionRef(eq(ORANGE_MONEY), any())).thenReturn(attemptTransactionOrange());
    when(mobileMoneyApi.getByTransactionRef(eq(AIRTEL_MONEY), any())).thenThrow(ApiException.class);
  }

  public TransactionDetails attemptTransactionTelma() {
    return TransactionDetails.builder()
            .pspTransactionRef("TELMA-ref")
            .pspTransactionAmount(300_000)
            .pspDatetimeTransactionCreation(Instant.parse("2021-11-08T08:25:24.00Z"))
            .build();
  }

  public TransactionDetails attemptTransactionOrange() {
    return TransactionDetails.builder()
            .pspTransactionRef("ORANGE-ref")
            .pspTransactionAmount(300_000)
            .pspDatetimeTransactionCreation(Instant.parse("2021-11-08T08:25:24.00Z"))
            .build();
  }

  private void setUpMpbsDao(MpbsDao mpbsDao) {
    when(mpbsDao.findMpbsBetween(any(Instant.class), any(Instant.class)))
        .thenReturn(List.of(expectedMpbs1Domain()));
  }

  @Test
  void mvola_mobile_money_service_is_correctly_executed() {
    CheckMobilePaymentTransactionTriggered checkMobilePaymentTriggered =
        CheckMobilePaymentTransactionTriggered.builder().build();
    subject.accept(checkMobilePaymentTriggered);

    verify(mvolaApiMock, times(1)).getByTransactionRef(MVOLA, "psp2_id");
  }

  @Test
  void student_read_own_mpbs_verifications_ok() throws school.hei.haapi.endpoint.rest.client.ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    List<MpbsVerification> actual = api.getMpbsVerifications(STUDENT1_ID, FEE1_ID);

    assertEquals(expected1MpbsVerification(), actual.get(0));
    assertTrue(actual.contains(expected1MpbsVerification()));
  }

  @Test
void manager_read_mpbs_verification_ok() throws school.hei.haapi.endpoint.rest.client.ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    List<MpbsVerification> actual = api.getMpbsVerifications(STUDENT1_ID, FEE1_ID);

    assertTrue(actual.contains(expected1MpbsVerification()));
  }

  @Test
  void student_read_other_mpbs_verifications_ko() throws school.hei.haapi.endpoint.rest.client.ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsForbiddenException(() -> api.getMpbsVerifications(STUDENT2_ID, FEE2_ID));
  }

  public school.hei.haapi.model.Mpbs.MpbsVerification domain1MpbsVerification() {
    MpbsVerification rest1MpbsVerification = expected1MpbsVerification();
    school.hei.haapi.model.Mpbs.MpbsVerification domainMpbs =
        new school.hei.haapi.model.Mpbs.MpbsVerification();
    domainMpbs.setFee(domainFee1(fee1()));
    domainMpbs.setStudent(mapRestStudentToDomain(student1()));
    domainMpbs.setCreationDatetimeOfMpbs(rest1MpbsVerification.getCreationDatetimeOfMpbs());
    domainMpbs.setCreationDatetime(rest1MpbsVerification.getCreationDatetime());
    domainMpbs.setAmountInPsp(rest1MpbsVerification.getAmountInPsp());
    domainMpbs.setAmountOfFeeRemainingPayment(
        rest1MpbsVerification.getAmountOfFeeRemainingPayment());
    domainMpbs.setMobileMoneyType(rest1MpbsVerification.getPspType());
    domainMpbs.setComment(rest1MpbsVerification.getComment());
    domainMpbs.setCreationDatetimeOfPaymentInPsp(
        rest1MpbsVerification.getCreationDatetimeOfPaymentInPsp());

    return domainMpbs;
  }

  public MpbsVerification expected1MpbsVerification() {
    return new MpbsVerification()
        .studentId(STUDENT1_ID)
        .feeId(FEE1_ID)
        .pspId("psp3_id")
        .pspType(MVOLA)
        .comment("comment 1")
        .amountInPsp(8000)
        .amountOfFeeRemainingPayment(8000)
        .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .creationDatetimeOfMpbs(Instant.parse("2021-11-08T08:25:24.00Z"))
        .creationDatetimeOfPaymentInPsp(Instant.parse("2021-11-08T08:25:24.00Z"));
  }

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, SERVER_PORT);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
