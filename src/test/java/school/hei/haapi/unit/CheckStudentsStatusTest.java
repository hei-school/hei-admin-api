package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.testData.FeeTestData.createFeeWithStatus;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.MpbsTestData.createableMpbsFromFeeIdForStudent;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;
import static school.hei.haapi.model.User.Status.ENABLED;
import static school.hei.haapi.model.User.Status.SUSPENDED;

import java.time.Instant;
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
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateFee;
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
import school.hei.haapi.service.FeeService;
import school.hei.haapi.service.MpbsService;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.event.CheckSuspendedStudentsStatusService;
import school.hei.haapi.service.event.SuspendStudentsWithOverdueFeesService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public class CheckStudentsStatusTest extends FacadeITMockedThirdParties {
  @Autowired private CheckSuspendedStudentsStatusService checkSuspendedStudentsStatusService;
  @Autowired private SuspendStudentsWithOverdueFeesService suspendStudentsWithOverdueFeesService;
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private FeeRepository feeRepository;
  @Autowired private MpbsService mpbsService;
  @Autowired private FeeService feeService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @MockBean private VolaClient volaClientMock;

  private User manager;
  private User indebtedStudent;
  private User otherIndebtedStudent;
  private Fee lateFee1;
  private Fee lateFee2;
  private Fee otherLateFee;

  private String managerToken;
  private String indebtedStudentToken;

  /** Fees created through the API on top of the fixtures, swept in tearDown. */
  private final List<String> apiCreatedFeeIds = new ArrayList<>();

  @BeforeEach
  void setUp() {
    setUpEventBridge(eventBridgeClientMock);

    manager = userRepository.save(hasina());
    indebtedStudent = userRepository.save(axel());
    otherIndebtedStudent = userRepository.save(freddy());
    lateFee1 = feeRepository.save(someLateFee(indebtedStudent));
    lateFee2 = feeRepository.save(someLateFee(indebtedStudent));
    otherLateFee = feeRepository.save(someLateFee(otherIndebtedStudent));

    managerToken = tokenFor(casdoorAuthServiceMock, manager);
    indebtedStudentToken = tokenFor(casdoorAuthServiceMock, indebtedStudent);

    setUpVolaClient();
  }

  /** Registering an mpbs calls the PSP: the payment comes back as still being verified. */
  private void setUpVolaClient() {
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
    List<String> feeIds = new ArrayList<>(apiCreatedFeeIds);
    feeIds.addAll(List.of(lateFee1.getId(), lateFee2.getId(), otherLateFee.getId()));
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
    apiCreatedFeeIds.clear();
    userRepository.deleteAll(List.of(indebtedStudent, otherIndebtedStudent, manager));
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

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @Test
  @DirtiesContext
  void update_students_status_ok() {
    assertEquals(ENABLED, userService.getById(indebtedStudent.getId()).getStatus());

    // here, we check if the enabled student has paid all their fees
    suspendStudentsWithOverdueFeesService.suspendStudentsWithUnpaidOrLateFee();
    assertEquals(SUSPENDED, userService.getById(indebtedStudent.getId()).getStatus());

    feeService.computeRemainingAmount(lateFee1.getId(), 5000);
    feeService.computeRemainingAmount(lateFee2.getId(), 5000);

    // here, we check if the suspended student has paid all their fees
    checkSuspendedStudentsStatusService.updateStatusBasedOnPayment();
    assertEquals(ENABLED, userService.getById(indebtedStudent.getId()).getStatus());
  }

  @Test
  @DirtiesContext
  void pending_students_status_ok() throws ApiException {
    var managerPayingApi = new PayingApi(anApiClient(managerToken));
    var studentPayingApi = new PayingApi(anApiClient(indebtedStudentToken));
    var studentId = indebtedStudent.getId();

    assertEquals(ENABLED, userService.getById(studentId).getStatus());

    var studentFee =
        managerPayingApi.createStudentFees(studentId, List.of(someCreatableFee())).getFirst();
    apiCreatedFeeIds.add(studentFee.getId());
    studentPayingApi.crupdateMpbs(
        studentId,
        studentFee.getId(),
        createableMpbsFromFeeIdForStudent(studentId, studentFee.getId()));

    suspendStudentsWithOverdueFeesService.suspendStudentsWithUnpaidOrLateFee();

    // the student does have an unpaid or late fee
    assertTrue(userService.getStudentsWithLateFee().contains(userService.getById(studentId)));

    // yet they stay enabled, because of the pending mpbs
    assertEquals(1, mpbsService.countPendingOfStudent(studentId));
    assertEquals(ENABLED, userService.getById(studentId).getStatus());
  }

  @Test
  void get_all_students_with_unpaid_or_late_fee_ok() {
    var studentIdsWithUnpaidOrLateFee =
        userRepository.getStudentsWithLateFees().stream().map(User::getId).toList();

    assertTrue(studentIdsWithUnpaidOrLateFee.contains(indebtedStudent.getId()));
    assertTrue(studentIdsWithUnpaidOrLateFee.contains(otherIndebtedStudent.getId()));
  }
}
