package school.hei.haapi.unit;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;
import static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum.FAILED;
import static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum.SUCCEEDED;
import static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum.VERIFYING;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.PaidFeeByMpbsFailedNotificationBody;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.http.mapper.TransactionDetailsMapper;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.Payment;
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

  private MpbsVerificationService initMpbsVerificationService(
      UnverifiedMobilePaymentHandler unverifiedMobilePaymentHandlerMock,
      MobilePaymentService mobilePaymentService,
      TransactionDetailsMapper transactionDetailsMapper,
      ComputeVerifiedMobilePayment computeVerifiedMobilePayment) {
    return new MpbsVerificationService(
        mock(),
        mock(),
        mobilePaymentService,
        transactionDetailsMapper,
        mock(),
        mock(),
        unverifiedMobilePaymentHandlerMock,
        computeVerifiedMobilePayment,
        new CollectionUtils(),
        mock(),
        volaMapper);
  }

  @Test
  void verification_split_verification_for_mbps() {
    MpbsVerificationService subject =
        initMpbsVerificationService(
            unverifiedMobilePaymentHandlerMock,
            mobilePaymentServiceMock,
            transactionDetailsMapper,
            computeVerifiedMobilePaymentMock);

    var mbpsPending = someMpbs("pending", now(), null);
    var mpbsVerified = someMpbs("verified", now(), null);
    var correspondingMockTransactionsFromVerifiedMpbs =
        MobileTransactionDetails.builder()
            .pspTransactionRef(mpbsVerified.getPspId())
            .pspTransactionAmount(0)
            .status(SUCCESS)
            .build();
    MpbsVerification fakeComputedVerifiedMpbs = new MpbsVerification();
    when(mobilePaymentServiceMock.findAllTransactionByMpbs(anyList()))
        .thenReturn(List.of(correspondingMockTransactionsFromVerifiedMpbs));
    TransactionDetails transactionsFromVerifiedMpbs =
        transactionDetailsMapper.toRestMobileTransactionDetails(
            correspondingMockTransactionsFromVerifiedMpbs);
    when(computeVerifiedMobilePaymentMock.saveTheVerifiedMpbs(
            mpbsVerified, transactionsFromVerifiedMpbs))
        .thenReturn(fakeComputedVerifiedMpbs);

    List<MpbsVerification> verifiedMpbs =
        subject.verifyMobilePaymentAndSaveResult(List.of(mbpsPending, mpbsVerified));

    ArgumentCaptor<List<Mpbs>> argumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(unverifiedMobilePaymentHandlerMock, times(1)).accept(argumentCaptor.capture());
    List<Mpbs> mobilePaymentUnverified = argumentCaptor.getAllValues().getFirst();
    var saveVerifiedMpbsCaptor = ArgumentCaptor.forClass(Mpbs.class);
    verify(computeVerifiedMobilePaymentMock, times(1))
        .saveTheVerifiedMpbs(saveVerifiedMpbsCaptor.capture(), any());
    List<Mpbs> savedMpbs = saveVerifiedMpbsCaptor.getAllValues();
    assertEquals(1, savedMpbs.size());
    assertEquals(mpbsVerified, savedMpbs.getFirst());
    assertEquals(1, mobilePaymentUnverified.size());
    assertEquals(mbpsPending, mobilePaymentUnverified.getFirst());
    assertEquals(1, verifiedMpbs.size());
    assertEquals(fakeComputedVerifiedMpbs, verifiedMpbs.getFirst());
  }

  @Test
  void verification_skip_bad_mobile_payment() {
    MpbsVerificationService subject =
        initMpbsVerificationService(
            new UnverifiedMobilePaymentHandler(
                mock(), new FailedMobilePaymentNotification(eventProducerMock)),
            mobilePaymentServiceMock,
            transactionDetailsMapper,
            computeVerifiedMobilePaymentMock);

    var pendingCreationDatetime = now().minus(1, DAYS);
    var failedCreationDatetime = now().minus(6, DAYS);
    var student = User.builder().email("email@gmail.com").build();
    var fee = Fee.builder().id("feeId").student(student).build();
    var badMpbs = someMpbs("bad", failedCreationDatetime, fee, student, null);
    var mpbsVerified = someMpbs("verified", pendingCreationDatetime, fee, student);
    var mpbsFailed = someMpbs("pending", failedCreationDatetime, fee, student);
    var correspondingMockTransactionsFromVerifiedMpbs =
        MobileTransactionDetails.builder()
            .pspTransactionRef(mpbsVerified.getPspId())
            .pspTransactionAmount(0)
            .status(SUCCESS)
            .build();
    var fakeComputedVerifiedMpbs = new MpbsVerification();
    when(mobilePaymentServiceMock.findAllTransactionByMpbs(anyList()))
        .thenReturn(List.of(correspondingMockTransactionsFromVerifiedMpbs));
    when(computeVerifiedMobilePaymentMock.saveTheVerifiedMpbs(
            mpbsVerified,
            transactionDetailsMapper.toRestMobileTransactionDetails(
                correspondingMockTransactionsFromVerifiedMpbs)))
        .thenReturn(fakeComputedVerifiedMpbs);

    List<MpbsVerification> verifiedMpbs =
        subject.verifyMobilePaymentAndSaveResult(List.of(badMpbs, mpbsVerified, mpbsFailed));

    verify(computeVerifiedMobilePaymentMock, never()).saveTheVerifiedMpbs(eq(badMpbs), any());
    assertEquals(1, verifiedMpbs.size());
    assertEquals(fakeComputedVerifiedMpbs, verifiedMpbs.getFirst());

    ArgumentCaptor<List<PaidFeeByMpbsFailedNotificationBody>> argumentCaptor =
        ArgumentCaptor.forClass(List.class);
    verify(eventProducerMock, times(1)).accept(argumentCaptor.capture());
    List<PaidFeeByMpbsFailedNotificationBody> notificationsRequestSend =
        argumentCaptor.getAllValues().getLast();
    assertEquals(1, notificationsRequestSend.size());
    assertEquals(
        PaidFeeByMpbsFailedNotificationBody.from(
            Payment.builder().fee(fee).amount(mpbsFailed.getAmount()).build()),
        notificationsRequestSend.getFirst());
  }

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
    var verifiedPaymentInfo = volaMapper.mpbsToPaymentInfos(mpbsVerified);
    var pendingPaymentInfo = volaMapper.mpbsToPaymentInfos(mbpsPending);

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
        subject.verifyMobilePaymentAndSaveResultWithVola(List.of(mbpsPending, mpbsVerified));

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

    var verifiedPaymentInfo = volaMapper.mpbsToPaymentInfos(mpbsVerified);
    var failedPaymentInfo = volaMapper.mpbsToPaymentInfos(mpbsFailed);
    var badPaymentInfo = volaMapper.mpbsToPaymentInfos(badMpbs);

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
        subject.verifyMobilePaymentAndSaveResultWithVola(
            List.of(badMpbs, mpbsVerified, mpbsFailed));

    verify(computeVerifiedMobilePaymentMock, times(1)).saveTheVerifiedMpbs(any(), any());
    verify(eventProducerMock, times(1)).accept(any());
    assertEquals(1, verifiedMpbs.size());
    assertEquals(fakeComputedVerifiedMpbs, verifiedMpbs.getFirst());
  }
}
