package school.hei.haapi.unit;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;
import static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum.SUCCEEDED;
import static school.hei.haapi.model.psp.vola.api.gen.client.model.PspPayment.PspTypeEnum.ORANGE_MONEY;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.PaidFeeByMpbsFailedNotificationBody;
import school.hei.haapi.endpoint.rest.mapper.MpbsMapper;
import school.hei.haapi.endpoint.rest.mapper.VolaMapper;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.http.mapper.TransactionDetailsMapper;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsVerification;
import school.hei.haapi.model.psp.vola.api.VolaClient;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PspPayment;
import school.hei.haapi.service.ComputeVerifiedMobilePayment;
import school.hei.haapi.service.FailedMobilePaymentNotification;
import school.hei.haapi.service.MobilePaymentService;
import school.hei.haapi.service.MpbsService;
import school.hei.haapi.service.MpbsVerificationService;
import school.hei.haapi.service.PaymentService;
import school.hei.haapi.service.UnverifiedMobilePaymentHandler;
import school.hei.haapi.service.utils.CollectionUtils;

class MpbsVerificationTest {
  MobilePaymentService mobilePaymentServiceMock = mock();
  UnverifiedMobilePaymentHandler unverifiedMobilePaymentHandlerMock = mock();
  ComputeVerifiedMobilePayment computeVerifiedMobilePaymentMock = mock();
  TransactionDetailsMapper transactionDetailsMapper = new TransactionDetailsMapper();
  EventProducer<PaidFeeByMpbsFailedNotificationBody> eventProducerMock = mock();
  VolaClient volaClientMock = mock();
  MpbsService mpbsServiceMock = mock();
  PaymentService paymentServiceMock = mock();
  MpbsMapper mpbsMapperMock = mock();
  MpbsVerificationService subject =
      initMpbsVerificationService(
          unverifiedMobilePaymentHandlerMock,
          mobilePaymentServiceMock,
          transactionDetailsMapper,
          computeVerifiedMobilePaymentMock);

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
        volaClientMock,
        new VolaMapper(),
        mpbsServiceMock,
        mpbsMapperMock,
        paymentServiceMock);
  }

  @Test
  void verification_split_verification_for_mbps() {
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
    MpbsVerificationService subjectWithRealHandler =
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
        subjectWithRealHandler.verifyMobilePaymentAndSaveResult(
            List.of(badMpbs, mpbsVerified, mpbsFailed));

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
  void verify_mpbs_from_vola_with_confirmed_payment() {
    var student = User.builder().email("dummy@gmail.com").build();
    var fee = Fee.builder().id("feeId").student(student).build();
    var mpbsToVerify =
        Mpbs.builder()
            .id("mpbs1")
            .pspId("MP260101.0000.B00000")
            .mobileMoneyType(MobileMoneyType.ORANGE_MONEY)
            .student(student)
            .fee(fee)
            .status(PENDING)
            .statusHistory(List.of())
            .build();
    var confirmedVolaPayment =
        school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.builder()
            .pspPayment(
                PspPayment.builder()
                    .pspType(ORANGE_MONEY)
                    .id("MP260101.0000.B00000")
                    .amount(10000)
                    .build())
            .verificationStatus(SUCCEEDED)
            .lastPspVerificationInstant(now().atOffset(java.time.ZoneOffset.UTC))
            .creationInstant(now().minus(1, DAYS).atOffset(java.time.ZoneOffset.UTC))
            .build();
    var savedMpbs =
        Mpbs.builder()
            .id("mpbs1")
            .pspId("MP260101.0000.B00000")
            .amount(10000)
            .status(SUCCESS)
            .student(student)
            .fee(fee)
            .statusHistory(List.of())
            .build();
    when(volaClientMock.get(ORANGE_MONEY, "MP260101.0000.B00000", "dummy@gmail.com"))
        .thenReturn(confirmedVolaPayment);
    when(mpbsServiceMock.save(any(Mpbs.class))).thenReturn(savedMpbs);

    Mpbs result = subject.verifyMpbsFromVola(mpbsToVerify);

    verify(volaClientMock, times(1)).get(ORANGE_MONEY, "MP260101.0000.B00000", "dummy@gmail.com");
    var mpbsSaveCaptor = ArgumentCaptor.forClass(Mpbs.class);
    verify(mpbsServiceMock, times(1)).save(mpbsSaveCaptor.capture());
    Mpbs mpbsPassedToSave = mpbsSaveCaptor.getValue();
    assertEquals(10000, mpbsPassedToSave.getAmount());
    assertEquals(SUCCESS, mpbsPassedToSave.getStatus());
    assertNotNull(mpbsPassedToSave.getSuccessfullyVerifiedOn());
    assertEquals(10000, result.getAmount());
    assertEquals(SUCCESS, result.getStatus());
  }

  @Test
  void verify_mpbs_from_vola_with_null_amount_returns_original() {
    var student = User.builder().email("dummy@gmail.com").build();
    var fee = Fee.builder().id("feeId").student(student).build();
    var mpbsToVerify =
        Mpbs.builder()
            .id("mpbs1")
            .pspId("MP260101.0000.B00000")
            .mobileMoneyType(MobileMoneyType.ORANGE_MONEY)
            .student(student)
            .fee(fee)
            .status(PENDING)
            .statusHistory(List.of())
            .build();
    var verifyingVolaPayment =
        school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.builder()
            .pspPayment(
                PspPayment.builder()
                    .pspType(ORANGE_MONEY)
                    .id("MP260101.0000.B00000")
                    .amount(null)
                    .build())
            .verificationStatus(
                school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum
                    .VERIFYING)
            .lastPspVerificationInstant(now().atOffset(java.time.ZoneOffset.UTC))
            .creationInstant(null)
            .build();
    when(volaClientMock.get(ORANGE_MONEY, "MP260101.0000.B00000", "dummy@gmail.com"))
        .thenReturn(verifyingVolaPayment);

    Mpbs result = subject.verifyMpbsFromVola(mpbsToVerify);

    verify(mpbsServiceMock, never()).save(any(Mpbs.class));
    assertEquals(mpbsToVerify, result);
  }

  @Test
  void send_vola_verification_request_and_save_result_ok() {
    var student = User.builder().id("studentId").email("dummy@gmail.com").build();
    var fee = Fee.builder().id("feeId").student(student).build();
    var mpbs =
        Mpbs.builder()
            .id("mpbs1")
            .pspId("MP260101.0000.B00000")
            .mobileMoneyType(MobileMoneyType.ORANGE_MONEY)
            .student(student)
            .fee(fee)
            .status(PENDING)
            .statusHistory(List.of())
            .build();
    var volaPaymentResponse =
        school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.builder()
            .pspPayment(
                PspPayment.builder()
                    .pspType(ORANGE_MONEY)
                    .id("MP260101.0000.B00000")
                    .amount(15000)
                    .build())
            .verificationStatus(SUCCEEDED)
            .lastPspVerificationInstant(now().atOffset(java.time.ZoneOffset.UTC))
            .creationInstant(now().minus(1, DAYS).atOffset(java.time.ZoneOffset.UTC))
            .build();
    var savedMpbs =
        Mpbs.builder()
            .id("mpbs1")
            .pspId("MP260101.0000.B00000")
            .amount(15000)
            .status(SUCCESS)
            .student(student)
            .fee(fee)
            .statusHistory(List.of())
            .build();
    var expectedRestMpbs = new school.hei.haapi.endpoint.rest.model.Mpbs().id("mpbs1");
    when(volaClientMock.create(ORANGE_MONEY, "MP260101.0000.B00000", "dummy@gmail.com"))
        .thenReturn(volaPaymentResponse);
    when(mpbsServiceMock.saveMpbs(any(Mpbs.class))).thenReturn(savedMpbs);
    when(mpbsMapperMock.toRest(savedMpbs)).thenReturn(expectedRestMpbs);

    var result = subject.sendVolaVerificationRequestAndSaveResult(mpbs);

    verify(volaClientMock, times(1))
        .create(ORANGE_MONEY, "MP260101.0000.B00000", "dummy@gmail.com");
    verify(mpbsServiceMock, times(1)).saveMpbs(any(Mpbs.class));
    verify(mpbsMapperMock, times(1)).toRest(savedMpbs);
    assertEquals("mpbs1", result.getId());
  }

  @Test
  void verify_mpbs_from_vola_with_refused_payment_is_still_saved() {
    var student = User.builder().email("dummy@gmail.com").build();
    var fee = Fee.builder().id("feeId").student(student).build();
    var mpbsToVerify =
        Mpbs.builder()
            .id("mpbs1")
            .pspId("MP260101.0000.B00000")
            .mobileMoneyType(MobileMoneyType.ORANGE_MONEY)
            .student(student)
            .fee(fee)
            .status(PENDING)
            .statusHistory(List.of())
            .build();
    var refusedVolaPayment =
        school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.builder()
            .pspPayment(
                PspPayment.builder()
                    .pspType(ORANGE_MONEY)
                    .id("MP260101.0000.B00000")
                    .amount(5000)
                    .build())
            .verificationStatus(
                school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum
                    .FAILED)
            .lastPspVerificationInstant(now().atOffset(java.time.ZoneOffset.UTC))
            .creationInstant(now().minus(1, DAYS).atOffset(java.time.ZoneOffset.UTC))
            .build();
    var savedMpbs =
        Mpbs.builder()
            .id("mpbs1")
            .amount(5000)
            .status(school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED)
            .student(student)
            .fee(fee)
            .statusHistory(List.of())
            .build();
    when(volaClientMock.get(ORANGE_MONEY, "MP260101.0000.B00000", "dummy@gmail.com"))
        .thenReturn(refusedVolaPayment);
    when(mpbsServiceMock.save(any(Mpbs.class))).thenReturn(savedMpbs);

    Mpbs result = subject.verifyMpbsFromVola(mpbsToVerify);

    var mpbsSaveCaptor = ArgumentCaptor.forClass(Mpbs.class);
    verify(mpbsServiceMock, times(1)).save(mpbsSaveCaptor.capture());
    Mpbs mpbsPassedToSave = mpbsSaveCaptor.getValue();
    assertEquals(
        school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED, mpbsPassedToSave.getStatus());
    assertEquals(5000, mpbsPassedToSave.getAmount());
    assertEquals(school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED, result.getStatus());
  }

  @Test
  void send_vola_verification_request_propagates_exception() {
    var student = User.builder().email("dummy@gmail.com").build();
    var fee = Fee.builder().id("feeId").student(student).build();
    var mpbs =
        Mpbs.builder()
            .id("mpbs1")
            .pspId("MP260101.0000.B00000")
            .mobileMoneyType(MobileMoneyType.ORANGE_MONEY)
            .student(student)
            .fee(fee)
            .status(PENDING)
            .statusHistory(List.of())
            .build();
    when(volaClientMock.create(ORANGE_MONEY, "MP260101.0000.B00000", "dummy@gmail.com"))
        .thenThrow(new RuntimeException("Vola API unreachable"));

    assertThrows(
        RuntimeException.class, () -> subject.sendVolaVerificationRequestAndSaveResult(mpbs));
    verify(mpbsServiceMock, never()).saveMpbs(any(Mpbs.class));
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
