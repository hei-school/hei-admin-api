package school.hei.haapi.endpoint.event;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L1;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.YEARLY;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.testData.StudentTestData.axel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import school.hei.haapi.endpoint.event.model.PendingMpbsCheckRequested;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.FeeService;
import school.hei.haapi.service.MobilePaymentService;
import school.hei.haapi.service.MpbsService;
import school.hei.haapi.service.event.PendingMpbsCheckRequestedService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

class PendingMpbsCheckRequestedServiceTest extends FacadeITMockedThirdParties {
  @Autowired PendingMpbsCheckRequestedService subject;
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private MpbsService mpbsService;
  @MockBean private MobilePaymentService mobilePaymentService;
  @Autowired private FeeService feeService;
  @Autowired private FeeRepository feeRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  private User student;

  /** Fees this class creates, swept in tearDown along with everything hanging off them. */
  private final List<String> ownedFeeIds = new ArrayList<>();

  @BeforeEach
  void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    student = userRepository.save(axel());
  }

  @AfterEach
  void tearDown() {
    // Fee carries @SQLDelete, so repository deletes would only flag is_deleted: the cleanup has to
    // reach the tables directly, children first.
    ownedFeeIds.forEach(
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
    userRepository.deleteById(student.getId());
  }

  private Fee saveOwnedFee(Instant dueDatetime) {
    var fee = feeService.saveAll(List.of(someFeeFor(student, dueDatetime))).getFirst();
    ownedFeeIds.add(fee.getId());
    return fee;
  }

  @Test
  void verify_mpbs_to_unverified() {
    // due tomorrow: an unverified check must leave the fee exactly as it was
    var fee = saveOwnedFee(now().plus(1, DAYS));
    mpbsService.saveMpbs(pendingMpbs(fee));
    when(mobilePaymentService.findTransactionByMpbs(any())).thenReturn(empty());
    var actualFee = feeService.getById(fee.getId());

    assertDoesNotThrow(
        () ->
            subject.accept(
                PendingMpbsCheckRequested.builder()
                    .toVerify(
                        mpbsService
                            .getStudentMobilePaymentByFeeId(student.getId(), fee.getId())
                            .getFirst())
                    .verifyAt(now())
                    .build()));

    var updatedFee = feeService.getById(fee.getId());
    assertEquals(actualFee.getStatus(), updatedFee.getStatus());
    assertEquals(actualFee.getRemainingAmount(), updatedFee.getRemainingAmount());
  }

  @Test
  void verify_mpbs() {
    var fee = saveOwnedFee(now());
    var mpbs = mpbsService.saveMpbs(pendingMpbs(fee));

    when(mobilePaymentService.findTransactionByMpbs(any()))
        .thenReturn(
            Optional.of(
                TransactionDetails.builder()
                    .pspTransactionAmount(mpbs.getAmount())
                    .pspDatetimeTransactionCreation(now())
                    .pspOwnDatetimeVerification(now())
                    .pspTransactionRef("pspTransactionRef")
                    .status(SUCCESS)
                    .build()));

    assertDoesNotThrow(
        () ->
            subject.accept(
                PendingMpbsCheckRequested.builder().toVerify(mpbs).verifyAt(now()).build()));

    assertEquals(PAID, feeService.getById(fee.getId()).getStatus());
  }

  @Test
  void unverifed_mpbs_ok() {
    var toFailFee = saveOwnedFee(now());
    var toPendFee = saveOwnedFee(now());
    when(mobilePaymentService.findTransactionByMpbs(any())).thenReturn(empty());

    assertDoesNotThrow(
        () ->
            subject.accept(
                PendingMpbsCheckRequested.builder()
                    .toVerify(toFailMpbs(toFailFee))
                    .verifyAt(now())
                    .build()));
    assertEquals(LATE, feeService.getById(toFailFee.getId()).getStatus());

    assertDoesNotThrow(
        () ->
            subject.accept(
                PendingMpbsCheckRequested.builder()
                    .toVerify(toPendMpbs(toPendFee))
                    .verifyAt(now())
                    .build()));
    assertEquals(LATE, feeService.getById(toPendFee.getId()).getStatus());
  }

  private static Fee someFeeFor(User student, Instant dueDatetime) {
    return Fee.builder()
        .student(student)
        .category(L1)
        .status(UNPAID)
        .dueDatetime(dueDatetime)
        .comment("Dummy comment")
        .remainingAmount(100)
        .totalAmount(100)
        .frequency(YEARLY)
        .type(TUITION)
        .build();
  }

  private Mpbs pendingMpbs(Fee fee) {
    var mpbs =
        Mpbs.builder()
            .status(PENDING)
            .student(student)
            .fee(fee)
            .amount(fee.getRemainingAmount())
            .statusHistory(new ArrayList<>())
            .build();
    mpbs.setMobileMoneyType(ORANGE_MONEY);
    mpbs.setPspId(randomUUID().toString());
    return mpbs;
  }

  private Mpbs toFailMpbs(Fee fee) {
    return Mpbs.builder()
        .mobileMoneyType(ORANGE_MONEY)
        .creationDatetime(now().minus(3, DAYS))
        .amount(100)
        .student(student)
        .pspId(randomUUID().toString())
        .statusHistory(new ArrayList<>())
        .fee(fee)
        .build();
  }

  private Mpbs toPendMpbs(Fee fee) {
    return Mpbs.builder()
        .mobileMoneyType(ORANGE_MONEY)
        .creationDatetime(now())
        .pspId(randomUUID().toString())
        .amount(100)
        .student(student)
        .statusHistory(new ArrayList<>())
        .fee(fee)
        .build();
  }
}
