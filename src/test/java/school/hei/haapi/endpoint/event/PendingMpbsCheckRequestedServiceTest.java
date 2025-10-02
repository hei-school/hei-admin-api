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
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.model.PendingMpbsCheckRequested;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.repository.FeeRepository;
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

  private static final String MOCK_FEE_ID = "mock-fee";
  private static final String MOCK_STUDENT_FIRST_NAME = "MOCK";
  private static final String MOCK_STUDENT_LAST_NAME = "Student";
  private static final String MOCK_STUDENT_EMAIL = "mock@example.com";

  @BeforeEach
  void setUp() {
    setUpEventBridge(eventBridgeClientMock);
  }

  @Test
  @Disabled("TODO: dirty, create new student")
  void verify_mpbs_to_unverified() {
    var actualFee = feeService.getById(FEE1_ID);
    assertDoesNotThrow(
        () ->
            subject.accept(
                PendingMpbsCheckRequested.builder()
                    .toVerify(
                        mpbsService.getStudentMobilePaymentByFeeId(STUDENT1_ID, FEE1_ID).getFirst())
                    .verifyAt(now())
                    .build()));
    var updatedFee = feeService.getById(FEE1_ID);

    assertEquals(actualFee, updatedFee);
  }

  @Test
  @Disabled("TODO: dirty, create new student")
  void verify_mpbs() {
    var fee = feeService.saveAll(List.of(someFeeFor(mockStudent()))).getFirst();
    var mpbsCreated =
        Mpbs.builder()
            .status(PENDING)
            .student(mockStudent())
            .fee(fee)
            .amount(fee.getRemainingAmount())
            .statusHistory(new ArrayList<>())
            .build();
    mpbsCreated.setMobileMoneyType(ORANGE_MONEY);
    mpbsCreated.setPspId("----");
    var mpbs = mpbsService.saveMpbs(mpbsCreated);

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
    feeRepository.delete(fee);
  }

  @Test
  @Disabled("TODO: dirty, create new student")
  void unverifed_mpbs_ok() {
    var toFailFee = feeService.saveAll(List.of(someFeeFor(mockStudent()))).getFirst();
    var toPendFee = feeService.saveAll(List.of(someFeeFor(mockStudent()))).getFirst();
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

  private static Fee someFeeFor(User student1) {
    return Fee.builder()
        .student(student1)
        .category(L1)
        .status(UNPAID)
        .dueDatetime(now())
        .comment("Dummy comment")
        .remainingAmount(100)
        .totalAmount(100)
        .frequency(YEARLY)
        .type(TUITION)
        .build();
  }

  private static Mpbs toFailMpbs(Fee fee) {
    return Mpbs.builder()
        .mobileMoneyType(ORANGE_MONEY)
        .creationDatetime(now().minus(3, DAYS))
        .amount(100)
        .student(mockStudent())
        .pspId(randomUUID().toString())
        .statusHistory(new ArrayList<>())
        .fee(fee)
        .build();
  }

  private static Mpbs toPendMpbs(Fee fee) {
    return Mpbs.builder()
        .mobileMoneyType(ORANGE_MONEY)
        .creationDatetime(now())
        .pspId(randomUUID().toString())
        .amount(100)
        .student(mockStudent())
        .statusHistory(new ArrayList<>())
        .fee(fee)
        .build();
  }

  private static User mockStudent() {
    return User.builder()
        .id(STUDENT1_ID)
        .firstName(MOCK_STUDENT_FIRST_NAME)
        .lastName(MOCK_STUDENT_LAST_NAME)
        .email(MOCK_STUDENT_EMAIL)
        .build();
  }
}
