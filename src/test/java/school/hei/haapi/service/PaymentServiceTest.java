package school.hei.haapi.service;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.SUSPENDED;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.FeeTestData.createFeeWithStatus;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.MpbsTestData.createCrupdateMpbs;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.CrupdateStudent;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.model.psp.vola.api.VolaClient;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PspPayment;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

class PaymentServiceTest extends FacadeITMockedThirdParties {
  @Autowired private PaymentService subject;
  @Autowired private MpbsService mpbsService;
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @MockBean private VolaClient volaClientMock;
  @Autowired private FeeService feeService;
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private FeeRepository feeRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  private User manager;
  private User studentWithLateFees;
  private User studentWithoutFees;
  private Fee lateFee1;
  private Fee lateFee2;
  private String managerToken;

  /** Everything created through the API on top of the fixtures, swept in tearDown. */
  private final List<String> ownedFeeIds = new ArrayList<>();

  private final List<String> ownedUserIds = new ArrayList<>();

  @BeforeEach
  void setUp() {
    setUpEventBridge(eventBridgeClientMock);

    manager = userRepository.save(hasina());
    studentWithLateFees = userRepository.save(axel());
    studentWithoutFees = userRepository.save(freddy());
    lateFee1 = feeRepository.save(someLateFee(studentWithLateFees));
    lateFee2 = feeRepository.save(someLateFee(studentWithLateFees));

    setUpS3Service(fileService, studentWithLateFees);
    managerToken = tokenFor(casdoorAuthServiceMock, manager);

    // registering an mpbs calls the PSP: the payment comes back as still being verified
    when(volaClientMock.create(any(PspPayment.PspTypeEnum.class), anyString(), anyString()))
        .thenAnswer(
            invocation ->
                Payment.builder()
                    .pspPayment(
                        PspPayment.builder()
                            .id(invocation.getArgument(1))
                            .pspType(PspPayment.PspTypeEnum.ORANGE_MONEY)
                            .build())
                    .verificationStatus(Payment.VerificationStatusEnum.VERIFYING)
                    .lastPspVerificationInstant(Instant.now().atOffset(ZoneOffset.UTC))
                    .creationInstant(Instant.now().atOffset(ZoneOffset.UTC))
                    .build());
  }

  @AfterEach
  void tearDown() {
    List<String> feeIds = new ArrayList<>(ownedFeeIds);
    feeIds.addAll(List.of(lateFee1.getId(), lateFee2.getId()));
    // Fee carries @SQLDelete, so a repository delete would only flag is_deleted: reach the tables
    // directly, children first.
    feeIds.forEach(
        feeId -> {
          jdbcTemplate.update(
              "DELETE FROM \"mpbs_status_history\" WHERE mpbs_id IN (SELECT id FROM \"mpbs\" WHERE"
                  + " fee_id = ?)",
              feeId);
          jdbcTemplate.update("DELETE FROM \"mpbs_verification\" WHERE fee_id = ?", feeId);

          jdbcTemplate.update("DELETE FROM \"mpbs\" WHERE fee_id = ?", feeId);
          jdbcTemplate.update("DELETE FROM \"fee_status_history\" WHERE fee_id = ?", feeId);
          jdbcTemplate.update("DELETE FROM \"payment\" WHERE fee_id = ?", feeId);
          jdbcTemplate.update("DELETE FROM \"fee\" WHERE id = ?", feeId);
        });
    ownedFeeIds.clear();

    userRepository.deleteAllById(ownedUserIds);
    ownedUserIds.clear();
    userRepository.deleteAll(List.of(studentWithLateFees, studentWithoutFees, manager));
  }

  private static Fee someLateFee(User student) {
    return createFeeWithStatus(student, 5_000, Instant.parse("2023-02-08T08:30:24.00Z"), LATE);
  }

  private static CreateFee someCreatableFee() {
    return new CreateFee()
        .type(TUITION)
        .totalAmount(5000)
        .category(UNKNOWN)
        .frequency(FeeFrequency.UNKNOWN)
        .comment("Comment")
        .dueDatetime(Instant.parse("2021-12-08T08:25:24.00Z"));
  }

  private static CrupdateStudent someCreatableStudent() {
    return new CrupdateStudent()
        .firstName("Some")
        .lastName("Student")
        .email("test+" + randomUUID() + "@hei.school")
        .ref("STD21" + randomUUID())
        .phone("0322411124")
        .coordinates(new Coordinates().latitude(-18.9).longitude(47.5))
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .birthDate(LocalDate.parse("2000-01-01"))
        .status(ENABLED);
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @Test
  void user_status_is_computed_after_paying_fee_by_mpbs() throws ApiException {
    var managerClient = anApiClient(managerToken);
    var usersApi = new UsersApi(managerClient);
    var payingApi = new PayingApi(managerClient);
    var studentId = studentWithLateFees.getId();

    var creatableStudent = someCreatableStudent();
    var createdFee = payingApi.createStudentFees(studentId, List.of(someCreatableFee())).getFirst();
    ownedFeeIds.add(createdFee.getId());
    var createdMpbs =
        payingApi.crupdateMpbs(
            studentId,
            createdFee.getId(),
            createCrupdateMpbs(
                studentId, createdFee.getId(), "MP240726.1541.D88426", ORANGE_MONEY));

    var createdStudent =
        usersApi.createOrUpdateStudents(List.of(creatableStudent), null).getFirst();
    ownedUserIds.add(createdStudent.getId());
    assertEquals(ENABLED, createdStudent.getStatus());

    creatableStudent.setId(createdStudent.getId());
    creatableStudent.setStatus(SUSPENDED);
    var suspendedStudent =
        usersApi.createOrUpdateStudents(List.of(creatableStudent), null).getFirst();
    assertEquals(SUSPENDED, suspendedStudent.getStatus());

    var domainMpbs = mpbsService.getByPspId(createdMpbs.getPspId());
    subject.savePaymentFromMpbs(domainMpbs, 5000);

    // the student now has paid every one of their late fees
    feeService.computeRemainingAmount(lateFee1.getId(), 5000);
    feeService.computeRemainingAmount(lateFee2.getId(), 5000);
    feeService.computeRemainingAmount(createdFee.getId(), 5000);

    assertEquals(ENABLED, usersApi.getStudentById(studentId).getStatus());
  }

  @Test
  @DirtiesContext
  void compute_user_status_after_paying_fee_ok() {
    feeService.computeUserStatusAfterPayingFee(studentWithLateFees);
    feeService.computeUserStatusAfterPayingFee(studentWithoutFees);

    assertEquals(
        User.Status.SUSPENDED, userService.getById(studentWithLateFees.getId()).getStatus());
    assertEquals(User.Status.ENABLED, userService.getById(studentWithoutFees.getId()).getStatus());

    // once every late fee is settled the student goes back to enabled
    feeService.computeRemainingAmount(lateFee1.getId(), 5000);
    feeService.computeRemainingAmount(lateFee2.getId(), 5000);
    feeService.computeUserStatusAfterPayingFee(studentWithLateFees);

    assertEquals(User.Status.ENABLED, userService.getById(studentWithLateFees.getId()).getStatus());
  }
}
