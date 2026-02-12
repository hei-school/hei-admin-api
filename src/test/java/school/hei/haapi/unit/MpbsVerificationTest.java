package school.hei.haapi.unit;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum.FAILED;
import static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum.SUCCEEDED;
import static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum.VERIFYING;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.PaidFeeByMpbsFailedNotificationBody;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.http.mapper.TransactionDetailsMapper;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsVerification;
import school.hei.haapi.model.psp.vola.api.VolaPsp;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PspPayment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.mapper.VolaMapper;
import school.hei.haapi.service.ComputeVerifiedMobilePayment;
import school.hei.haapi.service.FailedMobilePaymentNotification;
import school.hei.haapi.service.MobilePaymentService;
import school.hei.haapi.service.MpbsVerificationService;
import school.hei.haapi.service.UnverifiedMobilePaymentHandler;
import school.hei.haapi.service.utils.CollectionUtils;

class MpbsVerificationTest {
  VolaMapper volaMapper = new VolaMapper();
  MobilePaymentService mobilePaymentServiceMock = mock();
  UnverifiedMobilePaymentHandler unverifiedMobilePaymentHandlerMock = mock();
  ComputeVerifiedMobilePayment computeVerifiedMobilePaymentMock = mock();
  TransactionDetailsMapper transactionDetailsMapper = new TransactionDetailsMapper(volaMapper);
  EventProducer<PaidFeeByMpbsFailedNotificationBody> eventProducerMock = mock();

  private static Mpbs someMpbs(
      String pspId, Instant creationDateTime, Fee fee, User student, Integer amount) {
    return Mpbs.builder()
        .pspId(pspId)
        .creationDatetime(creationDateTime)
        .fee(fee)
        .student(student)
        .amount(amount)
        .mobileMoneyType(ORANGE_MONEY)
        .statusHistory(List.of())
        .build();
  }

  public static Mpbs someMpbs(
      String pspId, Instant creationDateTime, User student, MobileMoneyType mobileMoneyType) {
    return Mpbs.builder()
        .pspId(pspId)
        .creationDatetime(creationDateTime)
        .student(student)
        .statusHistory(List.of())
        .mobileMoneyType(mobileMoneyType)
        .build();
  }

  public static Mpbs someMpbs(String pspId, Instant creationDateTime, Fee fee, User student) {
    return someMpbs(pspId, creationDateTime, fee, student, 0);
  }

  public static Mpbs someMpbs(String pspId, Instant creationDateTime, User student) {
    Fee fee = mock();
    return someMpbs(pspId, creationDateTime, fee, student);
  }

  @Test
  void vola_verification_split_verification_for_mbps() {
    VolaPsp volaPspMock = mock();
    var subject =
        new MpbsVerificationService(
            mock(),
            mock(),
            mobilePaymentServiceMock,
            transactionDetailsMapper,
            mock(),
            mock(),
            unverifiedMobilePaymentHandlerMock,
            computeVerifiedMobilePaymentMock,
            new CollectionUtils(),
            volaPspMock,
            new VolaMapper());

    var mbpsPending =
        someMpbs("pending", now(), User.builder().email("Arandom@gmail.com").build(), ORANGE_MONEY);
    var mpbsVerified =
        someMpbs(
            "verified", now(), User.builder().email("Arandom@gmail.com").build(), ORANGE_MONEY);
    var verifiedPaymentInfo = volaMapper.mpbsToPaymentIds(mpbsVerified);
    var pendingPaymentInfo = volaMapper.mpbsToPaymentIds(mbpsPending);

    MpbsVerification fakeComputedVerifiedMpbs = new MpbsVerification();

    when(volaPspMock.getPayments(anyList()))
        .thenReturn(
            List.of(
                school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.builder()
                    .id("payment-verified-id")
                    .pspPayment(
                        PspPayment.builder()
                            .id(verifiedPaymentInfo.getPspPaymentId())
                            .pspType(volaMapper.toPspPaymentType(verifiedPaymentInfo.getPspType()))
                            .amount(10000)
                            .creationInstant(new Date())
                            .build())
                    .creationInstant(new Date())
                    .lastPspVerificationInstant(new Date())
                    .verificationStatus(SUCCEEDED)
                    .build(),
                school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.builder()
                    .id("payment-pending-id")
                    .pspPayment(
                        PspPayment.builder()
                            .id(pendingPaymentInfo.getPspPaymentId())
                            .pspType(volaMapper.toPspPaymentType(pendingPaymentInfo.getPspType()))
                            .amount(null)
                            .creationInstant(new Date())
                            .build())
                    .creationInstant(new Date())
                    .lastPspVerificationInstant(new Date())
                    .verificationStatus(VERIFYING)
                    .build()));

    when(computeVerifiedMobilePaymentMock.saveTheVerifiedMpbs(any(), any()))
        .thenReturn(fakeComputedVerifiedMpbs);

    List<MpbsVerification> verifiedMpbs =
        subject.verifyMobilePaymentAndSaveResult(List.of(mbpsPending, mpbsVerified));

    verify(computeVerifiedMobilePaymentMock, times(1)).saveTheVerifiedMpbs(any(), any());
    verify(unverifiedMobilePaymentHandlerMock, times(1)).accept(List.of(mbpsPending));
    assertEquals(1, verifiedMpbs.size());
    assertEquals(fakeComputedVerifiedMpbs, verifiedMpbs.getFirst());
  }

  @Test
  void vola_verification_skip_bad_mobile_payment() {
    VolaPsp volaPspMock = mock();
    var subject =
        new MpbsVerificationService(
            mock(),
            mock(),
            mobilePaymentServiceMock,
            transactionDetailsMapper,
            mock(),
            mock(),
            new UnverifiedMobilePaymentHandler(
                mock(), new FailedMobilePaymentNotification(eventProducerMock)),
            computeVerifiedMobilePaymentMock,
            new CollectionUtils(),
            volaPspMock,
            new VolaMapper());

    var student = User.builder().email("email@gmail.com").build();
    var fee = Fee.builder().id("feeId").student(student).build();
    var badMpbs = someMpbs("bad", now().minus(6, DAYS), fee, student, 500);
    var mpbsVerified = someMpbs("verified", now().minus(1, DAYS), fee, student, 1000);
    var mpbsFailed = someMpbs("failed", now().minus(6, DAYS), fee, student, 800);
    var fakeComputedVerifiedMpbs = new MpbsVerification();

    var verifiedPaymentInfo = volaMapper.mpbsToPaymentIds(mpbsVerified);
    var failedPaymentInfo = volaMapper.mpbsToPaymentIds(mpbsFailed);
    var badPaymentInfo = volaMapper.mpbsToPaymentIds(badMpbs);

    when(volaPspMock.getPayments(anyList()))
        .thenReturn(
            List.of(
                school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.builder()
                    .id("payment-verified-id")
                    .pspPayment(
                        PspPayment.builder()
                            .id(verifiedPaymentInfo.getPspPaymentId())
                            .pspType(volaMapper.toPspPaymentType(verifiedPaymentInfo.getPspType()))
                            .amount(1000)
                            .creationInstant(new Date())
                            .build())
                    .creationInstant(new Date())
                    .lastPspVerificationInstant(new Date())
                    .verificationStatus(SUCCEEDED)
                    .build(),
                school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.builder()
                    .id(null) // Payment FAILED retourné par Vola
                    .pspPayment(
                        PspPayment.builder()
                            .id(failedPaymentInfo.getPspPaymentId())
                            .pspType(volaMapper.toPspPaymentType(failedPaymentInfo.getPspType()))
                            .amount(null)
                            .creationInstant(null)
                            .build())
                    .creationInstant(null)
                    .lastPspVerificationInstant(null)
                    .verificationStatus(FAILED)
                    .build(),
                school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.builder()
                    .id(null) // Payment FAILED retourné par Vola
                    .pspPayment(
                        PspPayment.builder()
                            .id(badPaymentInfo.getPspPaymentId())
                            .pspType(volaMapper.toPspPaymentType(badPaymentInfo.getPspType()))
                            .amount(null)
                            .creationInstant(null)
                            .build())
                    .creationInstant(null)
                    .lastPspVerificationInstant(null)
                    .verificationStatus(FAILED)
                    .build()));

    when(computeVerifiedMobilePaymentMock.saveTheVerifiedMpbs(any(), any()))
        .thenReturn(fakeComputedVerifiedMpbs);

    List<MpbsVerification> verifiedMpbs =
        subject.verifyMobilePaymentAndSaveResult(List.of(badMpbs, mpbsVerified, mpbsFailed));

    verify(computeVerifiedMobilePaymentMock, times(1)).saveTheVerifiedMpbs(any(), any());
    verify(eventProducerMock, times(1)).accept(any());
    assertEquals(1, verifiedMpbs.size());
    assertEquals(fakeComputedVerifiedMpbs, verifiedMpbs.getFirst());
  }
}
