package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.SUSPENDED;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsApiException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.FeeTestData.createPendingFee;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.testData.PaymentTestData.aPayment;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.CreatePayment;
import school.hei.haapi.endpoint.rest.model.CrupdateStudent;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.endpoint.rest.model.PaymentStatus;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.PaymentRepository;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

class PaymentIT extends FacadeITMockedThirdParties {
  private static final Instant DUE_DATETIME = Instant.parse("2022-12-08T08:25:24.00Z");

  @Autowired EntityManager entityManager;
  @Autowired private UserRepository userRepository;
  @Autowired private FeeRepository feeRepository;
  @Autowired private PaymentRepository paymentRepository;
  @MockBean private EventBridgeClient eventBridgeClientMock;

  private User studentAxel;
  private User studentFreddy;
  private User monitorAxel;
  private User managerHasina;
  private User teacherToky;

  /** Half paid: 2000 + 3000 out of 10000. */
  private Fee axelPartlyPaidFee;

  /** Untouched, 5000 remaining — the room left for the "amount exceeds remaining" cases. */
  private Fee axelOpenFee;

  private Fee freddyOpenFee;
  private Payment axelCashPayment;
  private Payment axelMobileMoneyPayment;

  private String axelToken;
  private String freddyToken;
  private String monitorToken;
  private String managerToken;
  private String teacherToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    studentFreddy = userRepository.save(freddy());

    // the join table is keyed monitor_id -> student_id, so the followed students hang off the
    // monitor
    monitorAxel = monitorOfAxel();
    monitorAxel.setMonitors(new ArrayList<>(List.of(studentAxel)));
    monitorAxel = userRepository.save(monitorAxel);

    managerHasina = userRepository.save(hasina());
    teacherToky = userRepository.save(toky());

    axelPartlyPaidFee = feeRepository.save(createPendingFee(studentAxel, 10000, DUE_DATETIME));
    axelOpenFee = feeRepository.save(createPendingFee(studentAxel, 5000, DUE_DATETIME));
    freddyOpenFee = feeRepository.save(createPendingFee(studentFreddy, 5000, DUE_DATETIME));

    axelCashPayment =
        paymentRepository.save(
            aPayment(
                axelPartlyPaidFee,
                school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.CASH,
                2000,
                "Comment",
                Instant.parse("2022-11-08T08:25:24.00Z")));
    axelMobileMoneyPayment =
        paymentRepository.save(
            aPayment(
                axelPartlyPaidFee,
                school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.MOBILE_MONEY,
                3000,
                null,
                Instant.parse("2022-11-10T08:25:25.00Z")));

    axelPartlyPaidFee.setRemainingAmount(5000);
    axelPartlyPaidFee = feeRepository.save(axelPartlyPaidFee);
  }

  @BeforeEach
  void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    freddyToken = tokenFor(casdoorAuthServiceMock, studentFreddy);
    monitorToken = tokenFor(casdoorAuthServiceMock, monitorAxel);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
  }

  @AfterEach
  void tearDown() {
    var ownedFeeIds =
        List.of(axelPartlyPaidFee.getId(), axelOpenFee.getId(), freddyOpenFee.getId());
    paymentRepository.deleteAll(
        paymentRepository.findAll().stream()
            .filter(p -> p.getFee() != null && ownedFeeIds.contains(p.getFee().getId()))
            .toList());
    feeRepository.deleteAllById(ownedFeeIds);
    // drop the join rows before the users they point at
    monitorAxel.setMonitors(new ArrayList<>());
    userRepository.save(monitorAxel);
    userRepository.deleteAll(
        List.of(studentAxel, studentFreddy, monitorAxel, managerHasina, teacherToky));
  }

  private PayingApi apiAs(String token) {
    return new PayingApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  /** Reads a payment bypassing the JPA {@code isDeleted} filter. */
  private Payment getPaymentByIdWithoutJpaFiltering(String paymentId) {
    try {
      var q =
          entityManager.createNativeQuery("SELECT * FROM \"payment\" where id = ?", Payment.class);
      q.setParameter(1, paymentId);
      return (Payment) q.getSingleResult();
    } catch (NullPointerException e) {
      throw new school.hei.haapi.model.exception.ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  private static List<String> idsOf(List<school.hei.haapi.endpoint.rest.model.Payment> payments) {
    return payments.stream().map(payment -> payment.getId()).toList();
  }

  private static CreatePayment aCreatablePayment(int amount) {
    return new CreatePayment()
        .type(CreatePayment.TypeEnum.CASH)
        .amount(amount)
        .status(PaymentStatus.VALIDATE)
        .comment("Comment")
        .creationDatetime(Instant.parse("2022-11-08T08:25:24.00Z"));
  }

  @Test
  void student_read_ok() throws ApiException {
    var actual =
        apiAs(axelToken).getStudentPayments(studentAxel.getId(), axelPartlyPaidFee.getId(), 1, 5);

    assertTrue(idsOf(actual).contains(axelCashPayment.getId()));
    assertTrue(idsOf(actual).contains(axelMobileMoneyPayment.getId()));
  }

  @Test
  void monitor_read_own_followed_student_payment_ok() throws ApiException {
    var actual =
        apiAs(monitorToken)
            .getStudentPayments(studentAxel.getId(), axelPartlyPaidFee.getId(), 1, 5);

    assertTrue(idsOf(actual).contains(axelCashPayment.getId()));
    assertTrue(idsOf(actual).contains(axelMobileMoneyPayment.getId()));
  }

  @Test
  void manager_read_ok() throws ApiException {
    var actual =
        apiAs(managerToken)
            .getStudentPayments(studentAxel.getId(), axelPartlyPaidFee.getId(), 1, 5);

    assertTrue(idsOf(actual).contains(axelCashPayment.getId()));
    assertTrue(idsOf(actual).contains(axelMobileMoneyPayment.getId()));
  }

  @Test
  void manager_delete_payment_ok() throws ApiException {
    var api = apiAs(managerToken);

    var deletedPayment =
        api.deleteStudentFeePaymentById(
            studentAxel.getId(), axelPartlyPaidFee.getId(), axelCashPayment.getId());
    assertEquals(axelCashPayment.getId(), deletedPayment.getId());

    var actualFee = api.getStudentFeeById(studentAxel.getId(), axelPartlyPaidFee.getId());
    assertEquals(7000, actualFee.getRemainingAmount());

    var payments = api.getStudentPayments(studentAxel.getId(), axelPartlyPaidFee.getId(), 1, 5);
    assertFalse(idsOf(payments).contains(axelCashPayment.getId()));

    // soft delete: the row is still there, only flagged
    assertTrue(getPaymentByIdWithoutJpaFiltering(axelCashPayment.getId()).isDeleted());
  }

  @Test
  void student_read_ko() {
    var api = apiAs(axelToken);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentPayments(studentFreddy.getId(), freddyOpenFee.getId(), null, null));
  }

  @Test
  void monitor_read_other_student_payment_ko() {
    var api = apiAs(monitorToken);

    assertThrowsForbiddenException(
        () -> api.getStudentPayments(studentFreddy.getId(), freddyOpenFee.getId(), null, null));
  }

  @Test
  void teacher_read_ko() {
    var api = apiAs(teacherToken);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentPayments(studentFreddy.getId(), freddyOpenFee.getId(), null, null));
  }

  @Test
  void manager_write_with_bank_type_ok() throws ApiException {
    var api = apiAs(managerToken);
    var bankTransfer =
        new CreatePayment()
            .type(CreatePayment.TypeEnum.BANK_TRANSFER)
            .amount(2000)
            .comment("Comment")
            .status(PaymentStatus.VALIDATE)
            .creationDatetime(Instant.parse("2022-11-08T08:25:24.00Z"));

    var actual =
        api.createStudentPayments(
            studentFreddy.getId(), freddyOpenFee.getId(), List.of(bankTransfer));

    var reread = api.getStudentPayments(studentFreddy.getId(), freddyOpenFee.getId(), 1, 5);
    assertTrue(reread.containsAll(actual));
  }

  @Test
  void manager_write_ok() throws ApiException {
    var api = apiAs(managerToken);

    var actual =
        api.createStudentPayments(
            studentAxel.getId(), axelOpenFee.getId(), List.of(aCreatablePayment(2000)));

    var reread = api.getStudentPayments(studentAxel.getId(), axelOpenFee.getId(), 1, 5);
    assertTrue(reread.containsAll(actual));
  }

  @Test
  void student_is_now_enabled_after_paying_fee() throws ApiException {
    var managerClient = anApiClient(managerToken);
    var payingApi = new PayingApi(managerClient);
    var usersApi = new UsersApi(managerClient);

    var subject = aCreatableStudent();
    var student = usersApi.createOrUpdateStudents(List.of(subject), null).getFirst();
    assertEquals(ENABLED, student.getStatus());

    subject.setId(student.getId());
    subject.setStatus(SUSPENDED);
    var actualSuspended = usersApi.createOrUpdateStudents(List.of(subject), null).getFirst();
    assertEquals(SUSPENDED, actualSuspended.getStatus());

    var subjectId = student.getId();
    var createdFee = payingApi.createStudentFees(subjectId, List.of(aCreatableFee())).getFirst();

    payingApi.createStudentPayments(
        subjectId, createdFee.getId(), List.of(aCreatablePayment(5000)));

    assertEquals(PAID, payingApi.getStudentFeeById(subjectId, createdFee.getId()).getStatus());
    assertEquals(ENABLED, usersApi.getStudentById(subjectId).getStatus());

    feeRepository.deleteById(createdFee.getId());
    userRepository.deleteById(subjectId);
  }

  @Test
  void teacher_write_ko() {
    var api = apiAs(teacherToken);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createStudentPayments(studentAxel.getId(), axelOpenFee.getId(), List.of()));
  }

  @Test
  void manager_write_ko() throws ApiException {
    var api = apiAs(managerToken);
    var before = api.getStudentPayments(studentAxel.getId(), axelOpenFee.getId(), 1, 5);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Payment amount (8000)"
            + " exceeds fee remaining amount (5000)\"}",
        () ->
            api.createStudentPayments(
                studentAxel.getId(),
                axelOpenFee.getId(),
                List.of(aCreatablePayment(2000), aCreatablePayment(6000))));

    var after = api.getStudentPayments(studentAxel.getId(), axelOpenFee.getId(), 1, 5);
    assertEquals(0, before.size());
    assertEquals(before, after);
  }

  @Test
  void manager_write_with_some_bad_fields_ko() {
    var api = apiAs(managerToken);
    var noAmount = aCreatablePayment(2000).amount(null);
    var negativeAmount = aCreatablePayment(2000).amount(-1);

    var noAmountException =
        assertThrows(
            ApiException.class,
            () ->
                api.createStudentPayments(
                    studentAxel.getId(), axelOpenFee.getId(), List.of(noAmount)));
    var negativeAmountException =
        assertThrows(
            ApiException.class,
            () ->
                api.createStudentPayments(
                    studentAxel.getId(), axelOpenFee.getId(), List.of(negativeAmount)));

    assertTrue(noAmountException.getMessage().contains("Amount is mandatory"));
    assertTrue(negativeAmountException.getMessage().contains("Amount must be positive"));
  }

  @Test
  void manager_write_with_non_given_creation_datetime_ko() {
    var api = apiAs(managerToken);
    var noCreationDatetime = aCreatablePayment(2000).creationDatetime(null);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Creation datetime is mandatory\"}",
        () ->
            api.createStudentPayments(
                studentAxel.getId(), axelOpenFee.getId(), List.of(noCreationDatetime)));
  }

  @Test
  void manager_write_with_creation_datetime_after_current_time_ko() {
    var api = apiAs(managerToken);
    var inTheFuture = aCreatablePayment(2000).creationDatetime(Instant.now().plusSeconds(60));

    assertThrows(
        ApiException.class,
        () ->
            api.createStudentPayments(
                studentAxel.getId(), axelOpenFee.getId(), List.of(inTheFuture)));
  }

  @Test
  void manager_write_changes_expected() throws ApiException {
    var api = apiAs(managerToken);
    var fee = api.getStudentFeeById(studentAxel.getId(), axelOpenFee.getId());

    var actual =
        api.createStudentPayments(
            fee.getStudentId(), fee.getId(), List.of(aCreatablePayment(2000)));

    var reread = api.getStudentPayments(fee.getStudentId(), fee.getId(), 1, 10);
    var refreshedFee = api.getStudentFeeById(fee.getStudentId(), fee.getId());

    assertNotEquals(fee, refreshedFee);
    assertEquals(fee.getRemainingAmount() - 2000, refreshedFee.getRemainingAmount());
    assertEquals(reread, actual);
  }

  private static CreateFee aCreatableFee() {
    return new CreateFee()
        .type(TUITION)
        .totalAmount(5000)
        .category(UNKNOWN)
        .frequency(FeeFrequency.UNKNOWN)
        .comment("Comment")
        .dueDatetime(Instant.parse("2021-12-08T08:25:24.00Z"));
  }

  private static CrupdateStudent aCreatableStudent() {
    return new CrupdateStudent()
        .firstName("Nouveau")
        .lastName("Etudiant")
        .email("test+payment-" + java.util.UUID.randomUUID() + "@hei.school")
        .ref("STD" + java.util.UUID.randomUUID())
        .status(ENABLED)
        .sex(Sex.M)
        .birthDate(java.time.LocalDate.parse("2000-01-01"))
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .address("Adr 1")
        .coordinates(new Coordinates().longitude(10.0).latitude(10.0));
  }
}
