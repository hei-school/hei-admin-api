package school.hei.haapi.integration;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.MONTHLY;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.model.User.Role.STUDENT;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreatePayment;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;
import school.hei.haapi.endpoint.rest.model.PaymentStatus;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CreditRepository;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.FeeStatusHistoryRepository;
import school.hei.haapi.repository.PaymentRepository;
import school.hei.haapi.repository.TransactionRepository;
import school.hei.haapi.repository.UserRepository;

@Testcontainers
@AutoConfigureMockMvc
@Slf4j
class CreditControllerIT extends FacadeITMockedThirdParties {
  @Autowired FeeRepository feeRepository;
  @Autowired UserRepository userRepository;
  private static User student;
  private static Fee feeToArchive;
  private static Fee currentFee;
  @Autowired private CreditRepository creditRepository;
  @Autowired private TransactionRepository transactionRepository;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private FeeStatusHistoryRepository feeStatusHistoryRepository;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpTestData();
  }

  void setUpTestData() {
    student = userRepository.save(student());
    var savedFees = feeRepository.saveAll(List.of(feeToArchive(), currentFee()));
    feeToArchive = savedFees.getFirst();
    currentFee = savedFees.getLast();
  }

  @AfterEach
  void tearDown() {
    transactionRepository.deleteAll();
    creditRepository.deleteAll();
    paymentRepository.deleteAll();
    feeStatusHistoryRepository.deleteAll();
    feeRepository.deleteAllById(List.of(feeToArchive.getId(), currentFee.getId()));
    userRepository.deleteById(student.getId());
  }

  @Test
  void manager_archive_fee_OK() throws ApiException {
    setUpTestData();
    var anApiClient = anApiClient(MANAGER1_TOKEN);
    var payingApi = new PayingApi(anApiClient);
    var archivedFee = payingApi.archiveStudentFee(student.getId(), feeToArchive.getId());
    assertNotNull(archivedFee);
    assertEquals(true, archivedFee.getIsArchived());
  }

  @Test
  void student_read_credit_by_student_id_OK() throws ApiException {
    var anApiClient = anApiClient(STUDENT1_TOKEN);
    var payingApi = new PayingApi(anApiClient);
    var managerApiClient = anApiClient(MANAGER1_TOKEN);
    var managerPayingApi = new PayingApi(managerApiClient);
    managerPayingApi.archiveStudentFee(student.getId(), feeToArchive.getId());
    var credit = payingApi.getCreditByStudentId(student.getId());
    assertNotNull(credit);
    assertEquals(200000, credit.getAmount());
    assertEquals(student.getId(), credit.getStudent().getId());
    assertEquals(student.getRef(), credit.getStudent().getRef());
  }

  @Test
  void student_create_credit_payment_OK() throws ApiException {
    var anApiClient = anApiClient(STUDENT1_TOKEN);
    var payingApi = new PayingApi(anApiClient);
    var managerApiClient = anApiClient(MANAGER1_TOKEN);
    var managerPayingApi = new PayingApi(managerApiClient);
    managerPayingApi.archiveStudentFee(student.getId(), feeToArchive.getId());
    var payments =
        payingApi.createStudentPayments(
            student.getId(), currentFee.getId(), List.of(bankPayment(), creditPaymentCreated()));
    assertNotNull(payments);
  }

  @Test
  void manager_validate_credit_payments_OK() throws ApiException {
    var studentApiClient = anApiClient(STUDENT1_TOKEN);
    var managerApiClient = anApiClient(MANAGER1_TOKEN);
    var studentPayingApi = new PayingApi(studentApiClient);
    var managerPayingApi = new PayingApi(managerApiClient);
    managerPayingApi.archiveStudentFee(student.getId(), feeToArchive.getId());
    var payments =
        studentPayingApi.createStudentPayments(
            student.getId(), currentFee.getId(), List.of(bankPayment(), creditPaymentCreated()));
    var paymentsToValidate =
        managerPayingApi.getCreditPaymentsByStatus(PaymentStatus.CREATED, 1, 10);
    assertEquals(payments.getLast(), paymentsToValidate.getFirst());
    var creditPaymentsValidated =
        managerPayingApi.validateCreditPayments(List.of(paymentsToValidate.getFirst().getId()));
    assertNotNull(creditPaymentsValidated);
    var feePaid = managerPayingApi.getStudentFeeById(student.getId(), currentFee.getId());
    assertNotNull(feePaid);
    assertEquals(0, feePaid.getRemainingAmount());
    var actualCredit = managerPayingApi.getCreditByStudentId(student.getId());
    assertNotNull(actualCredit);
    assertEquals(150000, actualCredit.getAmount());
  }

  @Test
  void manager_reject_credit_payments_OK() throws ApiException {
    var studentApiClient = anApiClient(STUDENT1_TOKEN);
    var managerApiClient = anApiClient(MANAGER1_TOKEN);
    var studentPayingApi = new PayingApi(studentApiClient);
    var managerPayingApi = new PayingApi(managerApiClient);
    managerPayingApi.archiveStudentFee(student.getId(), feeToArchive.getId());
    var payments =
        studentPayingApi.createStudentPayments(
            student.getId(), currentFee.getId(), List.of(bankPayment(), creditPaymentCreated()));
    var paymentsToReject = managerPayingApi.getCreditPaymentsByStatus(PaymentStatus.CREATED, 1, 10);
    assertEquals(payments.getLast(), paymentsToReject.getFirst());
    var creditPaymentsRejected =
        managerPayingApi.rejectCreditPayments(List.of(paymentsToReject.getFirst().getId()));
    assertNotNull(creditPaymentsRejected);
    assertEquals(PaymentStatus.INVALIDATE, creditPaymentsRejected.getFirst().getStatus());
    var feeNotPaid = managerPayingApi.getStudentFeeById(student.getId(), currentFee.getId());
    assertNotNull(feeNotPaid);
    assertEquals(50000, feeNotPaid.getRemainingAmount());
    var actualCredit = managerPayingApi.getCreditByStudentId(student.getId());
    assertNotNull(actualCredit);
    assertEquals(200000, actualCredit.getAmount());
  }

  private static User student() {
    return User.builder()
        .ref("STD" + UUID.randomUUID())
        .firstName("John")
        .lastName("Doe")
        .status(User.Status.ENABLED)
        .email(UUID.randomUUID() + "@gmail.com")
        .entranceDatetime(Instant.parse("2025-11-15T00:00:00Z"))
        .role(STUDENT)
        .build();
  }

  private static Fee feeToArchive() {
    return Fee.builder()
        .student(student)
        .status(FeeStatusEnum.PAID)
        .type(FeeTypeEnum.TUITION)
        .totalAmount(200_000)
        .remainingAmount(0)
        .dueDatetime(Instant.parse("2025-12-15T00:00:00Z"))
        .isArchived(false)
        .frequency(MONTHLY)
        .mobilePayments(List.of())
        .build();
  }

  private static Fee currentFee() {
    return Fee.builder()
        .id("fee-2")
        .student(student)
        .status(FeeStatusEnum.UNPAID)
        .type(FeeTypeEnum.TUITION)
        .totalAmount(150_000)
        .remainingAmount(150_000)
        .dueDatetime(now().plus(10, DAYS))
        .isArchived(false)
        .frequency(MONTHLY)
        .build();
  }

  private static CreatePayment bankPayment() {
    return new CreatePayment()
        .type(CreatePayment.TypeEnum.BANK_TRANSFER)
        .status(PaymentStatus.VALIDATE)
        .amount(100_000)
        .comment("Bank payment")
        .creationDatetime(Instant.parse("2025-12-10T10:00:00Z"));
  }

  private static CreatePayment creditPaymentCreated() {
    return new CreatePayment()
        .type(CreatePayment.TypeEnum.CREDIT)
        .status(PaymentStatus.CREATED)
        .amount(50_000)
        .comment("Waiting manager validation")
        .creationDatetime(Instant.parse("2026-01-10T09:00:00Z"));
  }
}
