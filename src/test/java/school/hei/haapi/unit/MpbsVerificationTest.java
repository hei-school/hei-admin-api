package school.hei.haapi.unit;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.PaidFeeByMpbsFailedNotificationBody;
import school.hei.haapi.http.mapper.ExternalResponseMapper;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsVerification;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.service.ComputeVerifiedMobilePayement;
import school.hei.haapi.service.FailedMobilePaymentNotification;
import school.hei.haapi.service.MobilePaymentService;
import school.hei.haapi.service.MobilePaymentUnverifiedHandler;
import school.hei.haapi.service.MpbsVerificationService;

class MpbsVerificationTest {
  MobilePaymentService mobilePaymentServiceMock = mock();
  MobilePaymentUnverifiedHandler mobilePaymentUnverifiedHandlerMock = mock();
  ComputeVerifiedMobilePayement computeVerifiedMobilePayementMock = mock();
  ExternalResponseMapper externalResponseMapper = new ExternalResponseMapper();
  EventProducer<PaidFeeByMpbsFailedNotificationBody> eventProducerMock = mock();
  MpbsVerificationService subject =
      new MpbsVerificationService(
          mock(),
          mock(),
          mobilePaymentServiceMock,
          externalResponseMapper,
          mock(),
          mock(),
          mobilePaymentUnverifiedHandlerMock,
          computeVerifiedMobilePayementMock);
  MpbsVerificationService subjectWithUnverifiedPayementHandler =
      new MpbsVerificationService(
          mock(),
          mock(),
          mobilePaymentServiceMock,
          externalResponseMapper,
          mock(),
          mock(),
          new MobilePaymentUnverifiedHandler(
              mock(), new FailedMobilePaymentNotification(eventProducerMock)),
          computeVerifiedMobilePayementMock);

  @Test
  void verification_split_verification_for_mbps() {
    var mbpsPending = Mpbs.builder().pspId("pending").fee(mock()).build();
    var mpbsVerified = Mpbs.builder().pspId("verified").fee(mock()).build();
    var correspondingMockTransactionsFromVerifiedMpbs =
        MobileTransactionDetails.builder()
            .pspTransactionRef(mpbsVerified.getPspId())
            .pspTransactionAmount(0)
            .build();
    MpbsVerification fakeComputedVerifiedMpbs = new MpbsVerification();
    when(mobilePaymentServiceMock.findAllTransactionByMpbsWithoutException(anyList()))
        .thenReturn(List.of(correspondingMockTransactionsFromVerifiedMpbs));
    TransactionDetails transactionsFromVerifiedMpbs =
        externalResponseMapper.toRestMobileTransactionDetails(
            correspondingMockTransactionsFromVerifiedMpbs);
    when(computeVerifiedMobilePayementMock.saveTheVerifiedMpbs(
            mpbsVerified, transactionsFromVerifiedMpbs))
        .thenReturn(fakeComputedVerifiedMpbs);

    List<MpbsVerification> verifiedMpbs =
        subject.verifyMobilePaymentAndSaveResult(List.of(mbpsPending, mpbsVerified));

    ArgumentCaptor<List<Mpbs>> argumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(mobilePaymentUnverifiedHandlerMock, times(1)).accept(argumentCaptor.capture());
    List<Mpbs> mobilePaymentUnverified = argumentCaptor.getAllValues().getFirst();
    verify(computeVerifiedMobilePayementMock, never()).saveTheVerifiedMpbs(eq(mbpsPending), any());
    assertEquals(1, mobilePaymentUnverified.size());
    assertEquals(mbpsPending, mobilePaymentUnverified.getFirst());
    assertEquals(1, verifiedMpbs.size());
    assertEquals(fakeComputedVerifiedMpbs, verifiedMpbs.getFirst());
  }

  @Test
  void verification_skip_bad_mobile_payment() {
    var pendingCreationDatetime = Instant.now().minus(1, DAYS);
    var failedCreationDatetime = Instant.now().minus(4, DAYS);
    var student = User.builder().email("email@gmail.com").build();
    var fee = Fee.builder().id("feeId").student(student).build();
    var badMpbs =
        Mpbs.builder().pspId("bad").creationDatetime(failedCreationDatetime).fee(fee).build();
    var mpbsVerified = someMpbs("verified", pendingCreationDatetime, fee, student);
    var mpbsFailed = someMpbs("pending", failedCreationDatetime, fee, student);
    var correspondingMockTransactionsFromVerifiedMpbs =
        MobileTransactionDetails.builder()
            .pspTransactionRef(mpbsVerified.getPspId())
            .pspTransactionAmount(0)
            .build();
    var fakeComputedVerifiedMpbs = new MpbsVerification();
    when(mobilePaymentServiceMock.findAllTransactionByMpbsWithoutException(anyList()))
        .thenReturn(List.of(correspondingMockTransactionsFromVerifiedMpbs));
    when(computeVerifiedMobilePayementMock.saveTheVerifiedMpbs(
            mpbsVerified,
            externalResponseMapper.toRestMobileTransactionDetails(
                correspondingMockTransactionsFromVerifiedMpbs)))
        .thenReturn(fakeComputedVerifiedMpbs);

    List<MpbsVerification> verifiedMpbs =
        subjectWithUnverifiedPayementHandler.verifyMobilePaymentAndSaveResult(
            List.of(badMpbs, mpbsVerified, mpbsFailed));

    verify(computeVerifiedMobilePayementMock, never()).saveTheVerifiedMpbs(eq(badMpbs), any());
    assertEquals(1, verifiedMpbs.size());
    assertEquals(fakeComputedVerifiedMpbs, verifiedMpbs.getFirst());

    ArgumentCaptor<List<PaidFeeByMpbsFailedNotificationBody>> argumentCaptor =
        ArgumentCaptor.forClass(List.class);
    verify(eventProducerMock, times(1)).accept(argumentCaptor.capture());
    List<PaidFeeByMpbsFailedNotificationBody> notificationsRequestSend =
        argumentCaptor.getAllValues().getLast();
    assertEquals(2, notificationsRequestSend.size());
    assertNull(notificationsRequestSend.getFirst());
    assertEquals(
        PaidFeeByMpbsFailedNotificationBody.from(
            Payment.builder().fee(fee).amount(mpbsFailed.getAmount()).build()),
        notificationsRequestSend.get(1));
  }

  private Mpbs someMpbs(String pspId, Instant creationDateTime, Fee fee, User student) {
    return Mpbs.builder()
        .pspId(pspId)
        .creationDatetime(creationDateTime)
        .fee(fee)
        .student(student)
        .amount(0)
        .build();
  }
}
