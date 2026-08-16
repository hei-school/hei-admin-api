package school.hei.haapi.integration;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.FeeTestData.createPendingFee;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.testData.MpbsTestData.createCrupdateMpbs;
import static school.hei.haapi.integration.testData.MpbsTestData.createPendingMpbs;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;
import static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum.SUCCEEDED;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.CrupdateMpbs;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeStatusHistory;
import school.hei.haapi.model.User;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.psp.vola.api.VolaClient;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PspPayment;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.FeeStatusHistoryRepository;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.MpbsVerificationService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public class MpbsIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @MockBean private VolaClient volaClientMock;

  @Autowired private FeeRepository feeRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private MpbsRepository mpbsRepository;
  @Autowired private MpbsVerificationService mpbsVerificationService;
  @Autowired private FeeStatusHistoryRepository feeStatusHistoryRepository;

  private User studentAxel;
  private User studentFreddy;
  private User monitorAxel;
  private User managerHasina;
  private Fee axelFee;
  private Fee freddyFee;
  private Mpbs axelMpbs;

  /** Fees the tests create through the API, swept in tearDown. */
  private final List<String> createdFeeIds = new ArrayList<>();

  private String axelToken;
  private String monitorToken;
  private String managerToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    studentFreddy = userRepository.save(freddy());
    managerHasina = userRepository.save(hasina());

    monitorAxel = monitorOfAxel();
    monitorAxel.setMonitors(new ArrayList<>(List.of(studentAxel)));
    monitorAxel = userRepository.save(monitorAxel);

    axelFee = feeRepository.save(createPendingFee(studentAxel, 50_000, now().plus(30, DAYS)));
    freddyFee = feeRepository.save(createPendingFee(studentFreddy, 50_000, now().plus(30, DAYS)));

    axelMpbs =
        mpbsRepository.save(createPendingMpbs("psp-" + randomUUID(), studentAxel, axelFee, 50_000));
  }

  @BeforeEach
  void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpTestData();
    setUpS3Service(fileService, studentAxel);
    setUpVolaClient();

    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    monitorToken = tokenFor(casdoorAuthServiceMock, monitorAxel);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
  }

  @AfterEach
  void tearDown() {
    List<String> ownedFeeIds = new ArrayList<>(createdFeeIds);
    ownedFeeIds.addAll(List.of(axelFee.getId(), freddyFee.getId()));

    mpbsRepository.deleteAll(
        mpbsRepository.findAll().stream()
            .filter(m -> m.getFee() != null && ownedFeeIds.contains(m.getFee().getId()))
            .toList());
    feeRepository.deleteAllById(ownedFeeIds);
    createdFeeIds.clear();

    monitorAxel.setMonitors(new ArrayList<>());
    userRepository.save(monitorAxel);
    userRepository.deleteAll(List.of(studentAxel, studentFreddy, monitorAxel, managerHasina));
  }

  private void setUpVolaClient() {
    when(volaClientMock.get(any(), anyString(), anyString()))
        .thenAnswer(
            invocation -> {
              String pspId = invocation.getArgument(1);
              return Payment.builder()
                  .pspPayment(
                      PspPayment.builder()
                          .id(pspId)
                          .pspType(PspPayment.PspTypeEnum.ORANGE_MONEY)
                          .amount(axelMpbs.getAmount())
                          .build())
                  .verificationStatus(SUCCEEDED)
                  .lastPspVerificationInstant(now().atOffset(ZoneOffset.UTC))
                  .creationInstant(now().minus(1, DAYS).atOffset(ZoneOffset.UTC))
                  .build();
            });
    when(volaClientMock.create(any(PspPayment.PspTypeEnum.class), anyString(), anyString()))
        .thenAnswer(
            invocation -> {
              String pspId = invocation.getArgument(1);
              return Payment.builder()
                  .pspPayment(
                      PspPayment.builder()
                          .id(pspId)
                          .pspType(PspPayment.PspTypeEnum.ORANGE_MONEY)
                          .build())
                  .verificationStatus(Payment.VerificationStatusEnum.VERIFYING)
                  .lastPspVerificationInstant(now().atOffset(ZoneOffset.UTC))
                  .creationInstant(now().atOffset(ZoneOffset.UTC))
                  .build();
            });
  }

  private PayingApi apiAs(String token) {
    return new PayingApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private void assertIsAxelMpbs(school.hei.haapi.endpoint.rest.model.Mpbs actual) {
    assertEquals(axelMpbs.getId(), actual.getId());
    assertEquals(axelMpbs.getPspId(), actual.getPspId());
    assertEquals(studentAxel.getId(), actual.getStudentId());
    assertEquals(axelFee.getId(), actual.getFeeId());
    assertEquals(ORANGE_MONEY, actual.getPspType());
    assertEquals(50_000, actual.getAmount());
    assertEquals(PENDING, actual.getStatus());
  }

  @Test
  void manager_read_student_mobile_money_ok() throws ApiException {
    var actual = apiAs(managerToken).getMpbs(studentAxel.getId(), axelFee.getId()).getFirst();

    assertIsAxelMpbs(actual);
  }

  @Test
  void student_read_own_mobile_money_ok() throws ApiException {
    var actual = apiAs(axelToken).getMpbs(studentAxel.getId(), axelFee.getId()).getFirst();

    assertIsAxelMpbs(actual);
  }

  @Test
  void monitor_read_own_followed_student_mobile_money_ok() throws ApiException {
    var actual = apiAs(monitorToken).getMpbs(studentAxel.getId(), axelFee.getId()).getFirst();

    assertIsAxelMpbs(actual);
  }

  @Test
  void student_read_others_ko() {
    var api = apiAs(axelToken);

    assertThrowsForbiddenException(() -> api.getMpbs(studentFreddy.getId(), freddyFee.getId()));
  }

  @Test
  void monitor_read_others_student_mobile_money_ko() {
    var api = apiAs(monitorToken);

    assertThrowsForbiddenException(() -> api.getMpbs(studentFreddy.getId(), freddyFee.getId()));
  }

  @Test
  void student_update_mobile_payment_ok() throws ApiException {
    var api = apiAs(axelToken);

    var before = api.getMpbs(studentAxel.getId(), axelFee.getId()).getFirst();
    assertIsAxelMpbs(before);

    var toUpdate =
        createCrupdateMpbs(
            studentAxel.getId(), axelFee.getId(), "MP240726.1541.D88426", ORANGE_MONEY);
    var updated = api.crupdateMpbs(studentAxel.getId(), axelFee.getId(), toUpdate);

    assertEquals(studentAxel.getId(), updated.getStudentId());
    assertEquals(axelFee.getId(), updated.getFeeId());
    assertEquals("MP240726.1541.D88426", updated.getPspId());
    assertEquals(ORANGE_MONEY, updated.getPspType());
  }

  @Test
  void student_create_mobile_payment_ok() throws ApiException {
    var studentApi = apiAs(axelToken);
    var managerApi = apiAs(managerToken);

    var createdFee =
        managerApi
            .createStudentFees(
                studentAxel.getId(),
                List.of(
                    new CreateFee()
                        .totalAmount(5000)
                        .dueDatetime(Instant.parse("2030-11-08T08:25:24.00Z"))
                        .category(UNKNOWN)
                        .frequency(FeeFrequency.UNKNOWN)
                        .type(TUITION)
                        .creationDatetime(now())
                        .comment("test")))
            .getFirst();
    createdFeeIds.add(createdFee.getId());
    assertEquals(studentAxel.getId(), createdFee.getStudentId());

    var actual =
        studentApi.crupdateMpbs(
            studentAxel.getId(),
            createdFee.getId(),
            createCrupdateMpbs(
                studentAxel.getId(), createdFee.getId(), "MP240726.1541.D88425", ORANGE_MONEY));

    assertEquals(studentAxel.getId(), actual.getStudentId());
    assertEquals("MP240726.1541.D88425", actual.getPspId());
    assertEquals(ORANGE_MONEY, actual.getPspType());

    var updatedFee = studentApi.getStudentFeeById(studentAxel.getId(), createdFee.getId());
    assertEquals(FeeStatusEnum.PENDING, updatedFee.getStatus());
  }

  @Test
  void student_create_mobile_payments_ok() throws ApiException {
    var payingApi = apiAs(managerToken);

    var studentFee = createFeeThroughApi(studentFreddy);
    createdFeeIds.add(studentFee.getId());

    payingApi.crupdateMpbs(
        studentFreddy.getId(),
        studentFee.getId(),
        createRandomMpbs(studentFreddy.getId(), studentFee.getId()));
    payingApi.crupdateMpbs(
        studentFreddy.getId(),
        studentFee.getId(),
        createRandomMpbs(studentFreddy.getId(), studentFee.getId()));

    var reread = payingApi.getStudentFeeById(studentFreddy.getId(), studentFee.getId());
    assertEquals(2, reread.getMpbs().size());
  }

  @Test
  void vola_payment_updates_matching_fee_status() {
    mpbsVerificationService.verifyMpbsFromVola(axelMpbs);

    var actualFee = feeRepository.findById(axelFee.getId()).orElseThrow();
    var lastStatus =
        feeStatusHistoryRepository.findByFeeId(actualFee.getId()).stream()
            .max(comparing(FeeStatusHistory::getDatetime))
            .map(FeeStatusHistory::getStatus)
            .orElseThrow();

    assertEquals(PAID, lastStatus);
  }

  private school.hei.haapi.endpoint.rest.model.Fee createFeeThroughApi(User student)
      throws ApiException {
    return apiAs(managerToken)
        .createStudentFees(
            student.getId(),
            List.of(
                new CreateFee()
                    .type(TUITION)
                    .totalAmount(5000)
                    .category(UNKNOWN)
                    .frequency(FeeFrequency.UNKNOWN)
                    .comment("Comment")
                    .dueDatetime(now())))
        .getFirst();
  }

  private static CrupdateMpbs createRandomMpbs(String studentId, String feeId) {
    var random = new Random();
    var pspId =
        String.format(
            "MP%06d.%04d.D%05d",
            random.nextInt(1_000_000), random.nextInt(10000), random.nextInt(100_000));
    return new CrupdateMpbs().studentId(studentId).feeId(feeId).pspId(pspId).pspType(ORANGE_MONEY);
  }
}
