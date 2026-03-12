package school.hei.haapi.unit;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
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
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.service.ComputeVerifiedMobilePayment;
import school.hei.haapi.service.FailedMobilePaymentNotification;
import school.hei.haapi.service.MobilePaymentService;
import school.hei.haapi.service.MpbsVerificationService;
import school.hei.haapi.service.UnverifiedMobilePaymentHandler;
import school.hei.haapi.service.utils.CollectionUtils;

class MpbsVerificationTest {
  MobilePaymentService mobilePaymentServiceMock = mock();
  UnverifiedMobilePaymentHandler unverifiedMobilePaymentHandlerMock = mock();
  ComputeVerifiedMobilePayment computeVerifiedMobilePaymentMock = mock();
  TransactionDetailsMapper transactionDetailsMapper = new TransactionDetailsMapper(mock());
  EventProducer<PaidFeeByMpbsFailedNotificationBody> eventProducerMock = mock();
  MpbsRepository mpbsRepositoryMock = mock();
  VolaPsp volaPspMock = mock();
  VolaMapper volaMapper = new VolaMapper();

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
        mock());
  }

  private MpbsVerificationService initMpbsVerificationServiceWithVola() {
    return new MpbsVerificationService(
        mock(),
        mpbsRepositoryMock,
        mock(),
        new TransactionDetailsMapper(volaMapper),
        mock(),
        mock(),
        unverifiedMobilePaymentHandlerMock,
        computeVerifiedMobilePaymentMock,
        new CollectionUtils(),
        volaPspMock,
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

  @Test
  void vola_verification_separates_succeeded_and_unverified() {
    MpbsVerificationService subject = initMpbsVerificationServiceWithVola();

    var student = User.builder().email("student@gmail.com").build();
    var fee = Fee.builder().id("fee1").student(student).build();
    var succeededMpbs = someVolaMpbs("psp-succeeded", now(), fee, student);
    var unverifiedMpbs = someVolaMpbs("psp-unverified", now(), fee, student);

    when(mpbsRepositoryMock.findAllByStatus(PENDING))
        .thenReturn(List.of(succeededMpbs, unverifiedMpbs));
    when(volaPspMock.getPayments(anyList()))
        .thenReturn(
            List.of(
                someVolaPayment("psp-succeeded", 288000, SUCCEEDED),
                someVolaPayment("psp-unverified", 288000, VERIFYING)));

    var fakeVerification = new MpbsVerification();
    when(computeVerifiedMobilePaymentMock.saveTheVerifiedMpbs(eq(succeededMpbs), any()))
        .thenReturn(fakeVerification);

    List<MpbsVerification> result = subject.verifyMobilePaymentAndSaveResultWithVola();

    assertEquals(1, result.size());
    assertEquals(fakeVerification, result.getFirst());

    ArgumentCaptor<List<Mpbs>> unverifiedCaptor = ArgumentCaptor.forClass(List.class);
    verify(unverifiedMobilePaymentHandlerMock, times(1)).accept(unverifiedCaptor.capture());
    List<Mpbs> unverifiedList = unverifiedCaptor.getValue();
    assertEquals(1, unverifiedList.size());
    assertEquals(unverifiedMpbs, unverifiedList.getFirst());
  }

  @Test
  void vola_verification_all_succeeded() {
    MpbsVerificationService subject = initMpbsVerificationServiceWithVola();

    var student = User.builder().email("student@gmail.com").build();
    var fee = Fee.builder().id("fee1").student(student).build();
    var mpbs1 = someVolaMpbs("psp-1", now(), fee, student);
    var mpbs2 = someVolaMpbs("psp-2", now(), fee, student);

    when(mpbsRepositoryMock.findAllByStatus(PENDING)).thenReturn(List.of(mpbs1, mpbs2));
    when(volaPspMock.getPayments(anyList()))
        .thenReturn(
            List.of(
                someVolaPayment("psp-1", 288000, SUCCEEDED),
                someVolaPayment("psp-2", 265000, SUCCEEDED)));

    var fakeVerification1 = new MpbsVerification();
    var fakeVerification2 = new MpbsVerification();
    when(computeVerifiedMobilePaymentMock.saveTheVerifiedMpbs(eq(mpbs1), any()))
        .thenReturn(fakeVerification1);
    when(computeVerifiedMobilePaymentMock.saveTheVerifiedMpbs(eq(mpbs2), any()))
        .thenReturn(fakeVerification2);

    List<MpbsVerification> result = subject.verifyMobilePaymentAndSaveResultWithVola();

    assertEquals(2, result.size());

    ArgumentCaptor<List<Mpbs>> unverifiedCaptor = ArgumentCaptor.forClass(List.class);
    verify(unverifiedMobilePaymentHandlerMock, times(1)).accept(unverifiedCaptor.capture());
    assertTrue(unverifiedCaptor.getValue().isEmpty());
  }

  @Test
  void vola_verification_all_failed() {
    MpbsVerificationService subject = initMpbsVerificationServiceWithVola();

    var student = User.builder().email("student@gmail.com").build();
    var fee = Fee.builder().id("fee1").student(student).build();
    var mpbs1 = someVolaMpbs("psp-1", now(), fee, student);

    when(mpbsRepositoryMock.findAllByStatus(PENDING)).thenReturn(List.of(mpbs1));
    when(volaPspMock.getPayments(anyList()))
        .thenReturn(List.of(someVolaPayment("psp-1", 288000, FAILED)));

    List<MpbsVerification> result = subject.verifyMobilePaymentAndSaveResultWithVola();

    assertTrue(result.isEmpty());

    ArgumentCaptor<List<Mpbs>> unverifiedCaptor = ArgumentCaptor.forClass(List.class);
    verify(unverifiedMobilePaymentHandlerMock, times(1)).accept(unverifiedCaptor.capture());
    assertEquals(1, unverifiedCaptor.getValue().size());
    assertEquals(mpbs1, unverifiedCaptor.getValue().getFirst());
  }

  @Test
  void vola_verification_marks_all_unverified_on_vola_error() {
    MpbsVerificationService subject = initMpbsVerificationServiceWithVola();

    var student = User.builder().email("student@gmail.com").build();
    var fee = Fee.builder().id("fee1").student(student).build();
    var mpbs1 = someVolaMpbs("psp-1", now(), fee, student);
    var mpbs2 = someVolaMpbs("psp-2", now(), fee, student);

    when(mpbsRepositoryMock.findAllByStatus(PENDING)).thenReturn(List.of(mpbs1, mpbs2));
    when(volaPspMock.getPayments(anyList()))
        .thenThrow(new RuntimeException("Vola API unavailable"));

    List<MpbsVerification> result = subject.verifyMobilePaymentAndSaveResultWithVola();

    assertTrue(result.isEmpty());

    ArgumentCaptor<List<Mpbs>> unverifiedCaptor = ArgumentCaptor.forClass(List.class);
    verify(unverifiedMobilePaymentHandlerMock, times(1)).accept(unverifiedCaptor.capture());
    assertEquals(2, unverifiedCaptor.getValue().size());
  }

  @Test
  void vola_verification_with_no_pending_mpbs() {
    MpbsVerificationService subject = initMpbsVerificationServiceWithVola();

    when(mpbsRepositoryMock.findAllByStatus(PENDING)).thenReturn(List.of());
    when(volaPspMock.getPayments(anyList())).thenReturn(List.of());

    List<MpbsVerification> result = subject.verifyMobilePaymentAndSaveResultWithVola();

    assertTrue(result.isEmpty());
  }

  private static Mpbs someVolaMpbs(String pspId, Instant creationDateTime, Fee fee, User student) {
    return Mpbs.builder()
        .pspId(pspId)
        .creationDatetime(creationDateTime)
        .fee(fee)
        .student(student)
        .amount(0)
        .mobileMoneyType(ORANGE_MONEY)
        .statusHistory(List.of())
        .build();
  }

  private static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment someVolaPayment(
      String pspId,
      int amount,
      school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum status) {
    return school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.builder()
        .pspPayment(PspPayment.builder().id(pspId).amount(amount).build())
        .creationInstant(new Date())
        .verificationStatus(status)
        .build();
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
}
