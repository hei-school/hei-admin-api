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
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.PaidFeeByMpbsFailedNotificationBody;
import school.hei.haapi.http.mapper.TransactionDetailsMapper;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsVerification;
import school.hei.haapi.service.ComputeVerifiedMobilePayment;
import school.hei.haapi.service.FailedMobilePaymentNotification;
import school.hei.haapi.service.MobilePaymentService;
import school.hei.haapi.service.MpbsVerificationService;
import school.hei.haapi.service.UnverifiedMobilePaymentHandler;
import school.hei.haapi.service.utils.CollectionUtils;
import school.hei.haapi.model.psp.vola.api.VolaPsp;

class MpbsVerificationTest {
  MobilePaymentService mobilePaymentServiceMock = mock();
  UnverifiedMobilePaymentHandler unverifiedMobilePaymentHandlerMock = mock();
  ComputeVerifiedMobilePayment computeVerifiedMobilePaymentMock = mock();
  TransactionDetailsMapper transactionDetailsMapper = new TransactionDetailsMapper();
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
        mock());
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
        .statusHistory(List.of())
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
  void verification_split_verification_for_mbps_with_vola() {
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
            volaPspMock);

    var mbpsPending = someMpbs("pending", now(), null);
    var mpbsVerified = someMpbs("verified", now(), null);
    var mpbsVerifiedFromVola =
        mpbsVerified.toBuilder().status(SUCCESS).amount(1000).build();
    MpbsVerification fakeComputedVerifiedMpbs = new MpbsVerification();
    when(volaPspMock.get(mpbsVerified)).thenReturn(mpbsVerifiedFromVola);
    when(volaPspMock.get(mbpsPending)).thenReturn(mbpsPending.toBuilder().status(PENDING).build());
    when(computeVerifiedMobilePaymentMock.saveTheVerifiedMpbs(eq(mpbsVerified), any()))
        .thenReturn(fakeComputedVerifiedMpbs);

    List<MpbsVerification> verifiedMpbs =
        subject.verifyMobilePaymentAndSaveResultWithVola(List.of(mbpsPending, mpbsVerified));

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
  void verification_skip_bad_mobile_payment_with_vola() {
    VolaPsp volaPspMock = mock();
    MpbsVerificationService subject =
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
            volaPspMock);

    var pendingCreationDatetime = now().minus(1, DAYS);
    var failedCreationDatetime = now().minus(6, DAYS);
    var student = User.builder().email("email@gmail.com").build();
    var fee = Fee.builder().id("feeId").student(student).build();
    var badMpbs = someMpbs("bad", failedCreationDatetime, fee, student, null);
    var mpbsVerified = someMpbs("verified", pendingCreationDatetime, fee, student);
    var mpbsFailed = someMpbs("pending", failedCreationDatetime, fee, student);
    var mpbsVerifiedFromVola =
        mpbsVerified.toBuilder().status(SUCCESS).amount(1000).build();
    var fakeComputedVerifiedMpbs = new MpbsVerification();
    when(volaPspMock.get(mpbsVerified)).thenReturn(mpbsVerifiedFromVola);
    when(volaPspMock.get(mpbsFailed)).thenReturn(mpbsFailed.toBuilder().status(FAILED).build());
    when(volaPspMock.get(badMpbs)).thenThrow(new RuntimeException("Vola API error"));
    when(computeVerifiedMobilePaymentMock.saveTheVerifiedMpbs(eq(mpbsVerified), any()))
        .thenReturn(fakeComputedVerifiedMpbs);

    List<MpbsVerification> verifiedMpbs =
        subject.verifyMobilePaymentAndSaveResultWithVola(List.of(badMpbs, mpbsVerified, mpbsFailed));

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
}
