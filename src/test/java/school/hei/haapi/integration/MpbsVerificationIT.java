package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
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
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpMobilePaymentApi;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.event.EventConsumer;
import school.hei.haapi.endpoint.event.gen.CheckMobilePaymentTransactionTriggered;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.MpbsVerification;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.repository.dao.MpbsDao;
import school.hei.haapi.service.MpbsVerificationService;
import school.hei.haapi.service.event.CheckMobilePaymentTransactionTriggeredService;
import school.hei.haapi.service.mobileMoney.MobileMoneyApiFacade;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = MpbsVerificationIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class MpbsVerificationIT extends MockedThirdParties {
  @Autowired EventConsumer eventConsumer;
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @MockBean private MobileMoneyApiFacade mobileMoneyApiFacade;
  @Autowired private CheckMobilePaymentTransactionTriggeredService subject;
  @MockBean private MpbsDao mpbsDaoMock;
  @MockBean private MpbsVerificationService mpbsVerificationService;

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());
    setUpMpbsDao();
    setUpMobilePaymentApi(mobileMoneyApiFacade);
  }

  private void setUpMpbsDao() {
    when(mpbsDaoMock.findMpbsBetween(
            Instant.parse("2021-11-05T08:25:24.00Z"), Instant.parse("2021-11-10T08:25:24.00Z")))
        .thenReturn(List.of(expectedMpbs1Domain()));
    when(mpbsVerificationService.checkMobilePaymentThenSaveVerification())
        .thenReturn(List.of(domain1MpbsVerification()));
  }

  @Test
  void mobile_money_successfully_verified() throws InterruptedException, JsonProcessingException {
    CheckMobilePaymentTransactionTriggered checkMobilePaymentTriggered =
        CheckMobilePaymentTransactionTriggered.builder().build();
    // Trying to invoke the CheckMobilePaymentTransactionTriggered with EventConsumer
    eventConsumer.accept(
        List.of(
            new EventConsumer.AcknowledgeableTypedEvent(
                new EventConsumer.TypedEvent(
                    "school.hei.haapi.endpoint.event.gen.CheckMobilePaymentTransactionTriggered",
                    checkMobilePaymentTriggered),
                () -> {})));

    subject.accept(checkMobilePaymentTriggered);

    verify(mobileMoneyApiFacade, times(1)).getByTransactionRef(MVOLA, "psp2_id");
  }

  @Test
  void mobile_money_successfully_verified_ko()
      throws InterruptedException, JsonProcessingException {
    CheckMobilePaymentTransactionTriggered checkMobilePaymentTriggered =
        CheckMobilePaymentTransactionTriggered.builder().build();
    subject.accept(checkMobilePaymentTriggered);

    verify(mobileMoneyApiFacade, times(1)).getByTransactionRef(ORANGE_MONEY, "psp2_id");
  }

  @Test
  void student_read_own_mpbs_verifications_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    List<MpbsVerification> actual = api.getMpbsVerifications(STUDENT1_ID, FEE1_ID);

    assertEquals(expected1MpbsVerification(), actual.get(0));
    assertTrue(actual.contains(expected1MpbsVerification()));
  }

  @Test
  void manager_read_mpbs_verification_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    List<MpbsVerification> actual = api.getMpbsVerifications(STUDENT1_ID, FEE1_ID);

    assertTrue(actual.contains(expected1MpbsVerification()));
  }

  @Test
  void student_read_other_mpbs_verifications_ko() throws ApiException {
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
