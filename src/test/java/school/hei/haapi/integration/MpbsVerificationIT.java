package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.AIRTEL_MONEY;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.MVOLA;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.FeeTestData.createPendingFee;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.MpbsVerificationTestData.aMpbsVerification;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.mpbs.MpbsVerification;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.MpbsVerificationRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.mobileMoney.MobileMoneyApi;

public class MpbsVerificationIT extends FacadeITMockedThirdParties {
  private static final Instant PAYMENT_DATETIME = Instant.parse("2021-11-08T08:25:24.00Z");

  @MockBean(name = "OrangeApi")
  MobileMoneyApi orangeApiMock;

  @MockBean(name = "MvolaApi")
  MobileMoneyApi mvolaApiMock;

  @Autowired private UserRepository userRepository;
  @Autowired private FeeRepository feeRepository;
  @Autowired private MpbsVerificationRepository mpbsVerificationRepository;

  private User studentAxel;
  private User studentFreddy;
  private User managerHasina;
  private Fee axelFee;
  private Fee freddyFee;
  private MpbsVerification axelVerification;

  private String axelToken;
  private String managerToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    studentFreddy = userRepository.save(freddy());
    managerHasina = userRepository.save(hasina());

    axelFee = feeRepository.save(createPendingFee(studentAxel, 8000, PAYMENT_DATETIME));
    freddyFee = feeRepository.save(createPendingFee(studentFreddy, 8000, PAYMENT_DATETIME));

    axelVerification =
        mpbsVerificationRepository.save(
            aMpbsVerification(studentAxel, axelFee, MVOLA, 8000, PAYMENT_DATETIME));
  }

  @BeforeEach
  public void setUp() {
    setUpTestData();
    setUpS3Service(fileService, studentAxel);
    setUpMobileMock(orangeApiMock);
    setUpMobileMock(mvolaApiMock);

    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
  }

  @AfterEach
  void tearDown() {
    mpbsVerificationRepository.deleteById(axelVerification.getId());
    feeRepository.deleteAll(List.of(axelFee, freddyFee));
    userRepository.deleteAll(List.of(studentAxel, studentFreddy, managerHasina));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private void setUpMobileMock(MobileMoneyApi mobileMoneyApi) {
    when(mobileMoneyApi.getByTransactionRef(eq(MVOLA), any()))
        .thenReturn(attemptTransactionTelma());
    when(mobileMoneyApi.getByTransactionRef(eq(ORANGE_MONEY), any()))
        .thenReturn(attemptTransactionOrange());
    when(mobileMoneyApi.getByTransactionRef(eq(AIRTEL_MONEY), any())).thenThrow(ApiException.class);
  }

  public TransactionDetails attemptTransactionTelma() {
    return TransactionDetails.builder()
        .pspTransactionRef("TELMA-ref")
        .pspTransactionAmount(300_000)
        .pspDatetimeTransactionCreation(PAYMENT_DATETIME)
        .build();
  }

  public TransactionDetails attemptTransactionOrange() {
    return TransactionDetails.builder()
        .pspTransactionRef("ORANGE-ref")
        .pspTransactionAmount(300_000)
        .pspDatetimeTransactionCreation(PAYMENT_DATETIME)
        .build();
  }

  private void assertIsAxelVerification(
      school.hei.haapi.endpoint.rest.model.MpbsVerification actual) {
    assertEquals(studentAxel.getId(), actual.getStudentId());
    assertEquals(axelFee.getId(), actual.getFeeId());
    assertEquals(axelVerification.getPspId(), actual.getPspId());
    assertEquals(MVOLA, actual.getPspType());
    assertEquals(8000, actual.getAmountInPsp());
    assertEquals(8000, actual.getAmountOfFeeRemainingPayment());
  }

  @Test
  void student_read_own_mpbs_verifications_ok()
      throws school.hei.haapi.endpoint.rest.client.ApiException {
    var api = new PayingApi(anApiClient(axelToken));

    var actual = api.getMpbsVerifications(studentAxel.getId(), axelFee.getId());

    assertEquals(1, actual.size());
    assertIsAxelVerification(actual.getFirst());
  }

  @Test
  void manager_read_mpbs_verification_ok()
      throws school.hei.haapi.endpoint.rest.client.ApiException {
    var api = new PayingApi(anApiClient(managerToken));

    var actual = api.getMpbsVerifications(studentAxel.getId(), axelFee.getId());

    assertTrue(
        actual.stream().anyMatch(v -> axelVerification.getPspId().equals(v.getPspId())),
        "the manager should see the verification of this test");
  }

  @Test
  void student_read_other_mpbs_verifications_ko() {
    var api = new PayingApi(anApiClient(axelToken));
    assertThrowsForbiddenException(
        () -> api.getMpbsVerifications(studentFreddy.getId(), freddyFee.getId()));
  }
}
