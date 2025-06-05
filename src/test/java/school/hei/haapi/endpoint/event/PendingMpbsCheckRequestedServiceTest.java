package school.hei.haapi.endpoint.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L1;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.YEARLY;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.event.model.PendingMpbsCheckRequested;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.service.FeeService;
import school.hei.haapi.service.MobilePaymentService;
import school.hei.haapi.service.MpbsService;
import school.hei.haapi.service.event.PendingMpbsCheckRequestedService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Testcontainers
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class PendingMpbsCheckRequestedServiceTest extends FacadeITMockedThirdParties {
  @Autowired PendingMpbsCheckRequestedService subject;
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private MpbsService mpbsService;
  @Autowired private MpbsRepository mpbsRepository;
  @MockBean private MobilePaymentService mobilePaymentService;
  @Autowired private FeeService feeService;

  @BeforeEach
  void setUp() {
    setUpEventBridge(eventBridgeClientMock);
  }

  @Test
  void verify_mpbs_to_unverified() {
    assertDoesNotThrow(
        () ->
            subject.accept(
                PendingMpbsCheckRequested.builder()
                    .toVerify(
                        mpbsService.getStudentMobilePaymentByFeeId(STUDENT1_ID, FEE1_ID).getFirst())
                    .verifyAt(Instant.now())
                    .build()));
  }

  @Test
  void verify_mpbs() {
    User student1 = User.builder().id(STUDENT1_ID).build();
    Fee fee = feeService.saveAll(List.of(someFeeFor(student1))).getFirst();
    Mpbs mpbsCreated =
        Mpbs.builder().student(student1).fee(fee).amount(fee.getRemainingAmount()).build();
    mpbsCreated.setMobileMoneyType(ORANGE_MONEY);
    mpbsCreated.setPspId("----");
    Mpbs mpbs = mpbsService.saveMpbs(mpbsCreated);
    when(mobilePaymentService.findTransactionByMpbsWithoutException(any()))
        .thenReturn(
            Optional.of(
                new MobileTransactionDetails(
                    null,
                    mpbs.getAmount(),
                    Instant.now(),
                    Instant.now(),
                    "pspTransactionRef",
                    SUCCESS)));
    assertDoesNotThrow(
        () ->
            subject.accept(
                PendingMpbsCheckRequested.builder()
                    .toVerify(mpbs)
                    .verifyAt(Instant.now())
                    .build()));
    mpbsRepository.delete(mpbs);
  }

  private static Fee someFeeFor(User student1) {
    return Fee.builder()
        .student(student1)
        .category(L1)
        .dueDatetime(Instant.now())
        .remainingAmount(100)
        .totalAmount(100)
        .frequency(YEARLY)
        .type(TUITION)
        .build();
  }
}
