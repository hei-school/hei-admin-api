package school.hei.haapi.integration;

import static java.time.Instant.now;
import static java.time.Month.APRIL;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.test_data.FeeTestData.createPendingFee;
import static school.hei.haapi.integration.test_data.MpbsTestData.createCrupdateMpbs;
import static school.hei.haapi.integration.test_data.MpbsTestData.createPendingMpbs;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.User.Sex.M;
import static school.hei.haapi.model.User.Status.ENABLED;
import static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum.SUCCEEDED;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.CrupdateMpbs;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.endpoint.rest.model.Mpbs;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.FeeStatusHistory;
import school.hei.haapi.model.User;
import school.hei.haapi.model.psp.vola.api.VolaClient;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PspPayment;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.FeeStatusHistoryRepository;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.MpbsVerificationService;
import school.hei.haapi.service.UserService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Testcontainers
@AutoConfigureMockMvc
@Slf4j
public class MpbsIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @MockBean private VolaClient volaClientMock;
  @Autowired private UserService userService;
  private User studentAxel;
  private school.hei.haapi.model.Fee testFee;
  private school.hei.haapi.model.mpbs.Mpbs mpbsForTestFee;
  @Autowired private FeeRepository feeRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private MpbsRepository mpbsRepository;
  @Autowired private MpbsVerificationService mpbsVerificationService;
  @Autowired private FeeStatusHistoryRepository feeStatusHistoryRepository;

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());
    setUpTestData();
    setUpVolaClient();
  }

  @AfterEach
  void tearDown() {
    mpbsRepository.delete(mpbsForTestFee);
    feeRepository.delete(testFee);
    userRepository.delete(studentAxel);
  }

  private void setUpVolaClient() {
    when(volaClientMock.get(any(), anyString(), anyString()))
        .thenAnswer(
            invocation -> {
              String pspId = invocation.getArgument(1);
              return school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.builder()
                  .pspPayment(
                      PspPayment.builder()
                          .id(pspId)
                          .pspType(PspPayment.PspTypeEnum.ORANGE_MONEY)
                          .amount(mpbsForTestFee.getAmount())
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

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    testFee =
        feeRepository.save(createPendingFee(studentAxel, 50_000, Instant.now().plus(30, DAYS)));
    mpbsForTestFee = mpbsRepository.save(createPendingMpbs("psp", studentAxel, testFee, 50_000));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @Test
  void manager_read_student_mobile_money_ok() throws ApiException {
    var manager1Client = anApiClient(MANAGER1_TOKEN);
    var api = new PayingApi(manager1Client);

    var actual = api.getMpbs(STUDENT1_ID, FEE1_ID).getFirst();

    assertEquals(expectedMpbs1(), actual);
  }

  @Test
  void student_read_own_mobile_money_ok() throws ApiException {
    var student1Client = anApiClient(STUDENT1_TOKEN);
    var api = new PayingApi(student1Client);

    var actualMpbs = api.getMpbs(STUDENT1_ID, FEE1_ID).getFirst();

    assertEquals(expectedMpbs1(), actualMpbs);
  }

  @Test
  void monitor_read_own_followed_student_mobile_money_ok() throws ApiException {
    var monitor1Client = anApiClient(MONITOR1_TOKEN);
    var api = new PayingApi(monitor1Client);

    var mpbsStudent1ForFee1 = api.getMpbs(STUDENT1_ID, FEE1_ID).getFirst();

    assertEquals(expectedMpbs1(), mpbsStudent1ForFee1);
  }

  @Test
  void student_read_others_ko() {
    var student1Client = anApiClient(STUDENT1_TOKEN);
    var api = new PayingApi(student1Client);

    assertThrowsForbiddenException(() -> api.getMpbs(STUDENT2_ID, FEE2_ID));
  }

  @Test
  void monitor_read_others_student_mobile_money_ko() {
    var monitor1Client = anApiClient(MONITOR1_TOKEN);
    var api = new PayingApi(monitor1Client);

    assertThrowsForbiddenException(() -> api.getMpbs(STUDENT2_ID, FEE2_ID));
  }

  @Test
  @Disabled("TODO: dirty, create new student")
  void student_update_mobile_payment_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    Mpbs actual0 = api.getMpbs(STUDENT1_ID, FEE1_ID).getFirst();
    assertEquals(expectedMpbs1(), actual0);

    Mpbs inUpdate = api.crupdateMpbs(STUDENT1_ID, FEE1_ID, updatableMpbs1());
    var updated = expectedMpbs1();
    updated.setPspId("MP240726.1541.D88426");
    updated.setPspType(ORANGE_MONEY);
    assertEquals(updated.getStudentId(), inUpdate.getStudentId());
    assertEquals(updated.getPspId(), inUpdate.getPspId());
    assertEquals(updated.getFeeId(), inUpdate.getFeeId());
    assertEquals(updated.getPspType(), inUpdate.getPspType());

    // Assert that one fee has mpbs
    Mpbs actual1 = api.getMpbs(STUDENT1_ID, FEE1_ID).getFirst();
    actual1.setCreationDatetime(actual1.getCreationDatetime().truncatedTo(MINUTES));
    inUpdate.setCreationDatetime(inUpdate.getCreationDatetime().truncatedTo(MINUTES));
    assertEquals(actual1, inUpdate);

    // Assert that when we get fees it not throws error 500
    var actualFee = api.getFeesByStudentId(STUDENT1_ID, 1, 10, null);
    assertEquals(7, actualFee.size());
  }

  @Test
  @Disabled("TODO: dirty, create new student")
  void student_create_mobile_payment_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi manager1Api = new PayingApi(manager1Client);

    var actualFee =
        manager1Api
            .createStudentFees(
                STUDENT1_ID,
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
    assertEquals(STUDENT1_ID, actualFee.getStudentId());

    Mpbs actual =
        api.crupdateMpbs(
            STUDENT1_ID,
            actualFee.getId(),
            createCrupdateMpbs(
                STUDENT1_ID, actualFee.getId(), "MP240726.1541.D88425", ORANGE_MONEY));

    assertEquals(createableMpbs1().getStudentId(), actual.getStudentId());
    assertEquals(createableMpbs1().getPspId(), actual.getPspId());
    assertEquals(createableMpbs1().getPspType(), actual.getPspType());

    var updatedFee = api.getStudentFeeById(STUDENT1_ID, actualFee.getId());

    assertEquals(FeeStatusEnum.PENDING, updatedFee.getStatus());
  }

  @Test
  void student_create_mobile_payments_ok() throws ApiException {
    var apiClient = anApiClient(MANAGER1_TOKEN);
    var payingApi = new PayingApi(apiClient);

    var savedStudent = createStudentForMobilePayments();
    var savedStudentFee = createFeeForMobilePayments(savedStudent);
    var toInsertUserMpbs1 = createRandomMpbs(savedStudent.getId(), savedStudentFee.getId());
    var toInsertUserMpbs2 = createRandomMpbs(savedStudent.getId(), savedStudentFee.getId());

    payingApi.crupdateMpbs(savedStudent.getId(), savedStudentFee.getId(), toInsertUserMpbs1);
    payingApi.crupdateMpbs(savedStudent.getId(), savedStudentFee.getId(), toInsertUserMpbs2);

    var studentFee = payingApi.getStudentFeeById(savedStudent.getId(), savedStudentFee.getId());
    assertEquals(2, studentFee.getMpbs().size());
  }

  private CrupdateMpbs createRandomMpbs(String studentId, String feeId) {
    var random = new Random();
    String pspId =
        String.format(
            "MP%06d.%04d.D%05d",
            random.nextInt(1_000_000), random.nextInt(10000), random.nextInt(100_000));
    return new CrupdateMpbs().studentId(studentId).feeId(feeId).pspId(pspId).pspType(ORANGE_MONEY);
  }

  private Fee createFeeForMobilePayments(User student) throws ApiException {
    var apiClient = anApiClient(MANAGER1_TOKEN);
    var payingApi = new PayingApi(apiClient);

    var toCreateStudentFee =
        new CreateFee()
            .type(TUITION)
            .totalAmount(5000)
            .category(UNKNOWN)
            .frequency(FeeFrequency.UNKNOWN)
            .comment("Comment")
            .dueDatetime(now());

    return payingApi.createStudentFees(student.getId(), List.of(toCreateStudentFee)).getFirst();
  }

  @Test
  void vola_payment_updates_matching_fee_status() {
    mpbsVerificationService.verifyMpbsFromVola(mpbsForTestFee);

    var actualFee = feeRepository.findById(testFee.getId()).get();
    var actualFeeStatusHistories = feeStatusHistoryRepository.findByFeeId(actualFee.getId());
    var actualFeeLastStatusHistory =
        actualFeeStatusHistories.stream()
            .max(comparing(FeeStatusHistory::getDatetime))
            .map(FeeStatusHistory::getStatus)
            .get();

    assertEquals(PAID, actualFeeLastStatusHistory);
  }

  private User createStudentForMobilePayments() {
    var randomStudent =
        User.builder()
            .email("test_student_create_mobile_payments@test.com")
            .firstName("Test")
            .lastName("Payment_multiple_mpbs")
            .address("Address")
            .birthDate(LocalDate.of(2004, APRIL, 20))
            .phone("+261 00 00 000 00")
            .ref("STD-mpbs-multiple")
            .sex(M)
            .entranceDatetime(now())
            .birthPlace("Birthplace")
            .highSchoolOrigin("High School Origin")
            .status(ENABLED)
            .role(STUDENT)
            .build();

    return userService.saveAll(List.of(randomStudent)).getFirst();
  }

  private static CrupdateMpbs updatableMpbs1() {
    return createCrupdateMpbs(STUDENT1_ID, FEE1_ID, "MP240726.1541.D88426", ORANGE_MONEY);
  }

  public static Mpbs expectedMpbs1() {
    return new Mpbs()
        .id("mpbs1_id")
        .pspId("psp2_id")
        .studentId(STUDENT1_ID)
        .feeId(FEE1_ID)
        .pspType(ORANGE_MONEY)
        .amount(8000)
        .successfullyVerifiedOn(Instant.parse("2021-11-08T08:25:24.00Z"))
        .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .status(PENDING);
  }

  private static CrupdateMpbs createableMpbs1() {
    return createCrupdateMpbs(STUDENT1_ID, FEE2_ID, "MP240726.1541.D88425", ORANGE_MONEY);
  }

  public static CrupdateMpbs createableMpbsFromFeeIdForStudent(String studentId, String feeId) {
    return createCrupdateMpbs(studentId, feeId, "MP240726.1541.D88425", ORANGE_MONEY);
  }
}
