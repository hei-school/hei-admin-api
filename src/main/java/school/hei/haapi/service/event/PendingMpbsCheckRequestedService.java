package school.hei.haapi.service.event;

import static java.time.Instant.now;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.MOBILE_MONEY;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.PaidFeeByMpbsFailedNotificationBody;
import school.hei.haapi.endpoint.event.model.PendingMpbsCheckRequested;
import school.hei.haapi.endpoint.event.model.PojaEvent;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.http.mapper.TransactionDetailsMapper;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsVerification;
import school.hei.haapi.model.Payment;
import school.hei.haapi.repository.MpbsVerificationRepository;
import school.hei.haapi.service.FeeService;
import school.hei.haapi.service.MobilePaymentService;
import school.hei.haapi.service.MpbsService;
import school.hei.haapi.service.PaymentService;

@Service
@AllArgsConstructor
@Slf4j
public class PendingMpbsCheckRequestedService implements Consumer<PendingMpbsCheckRequested> {
  private final TransactionDetailsMapper transactionDetailsMapper;
  private final MpbsVerificationRepository repository;
  private final MpbsService mpbsService;
  private final FeeService feeService;
  private final MobilePaymentService mobilePaymentService;
  private final PaymentService paymentService;
  private final EventProducer<PojaEvent> eventProducer;

  private Mpbs saveTheUnverifiedMpbs(Mpbs mpbs, Instant toCompare) {
    log.info("could not verify {}", mpbs.getId());
    mpbs.setLastVerificationDatetime(now());
    mpbs.setStatus(defineMpbsStatusWithoutOrangeTransactionDetails(mpbs, toCompare));
    return mpbsService.save(mpbs);
  }

  private MpbsVerification saveTheVerifiedMpbs(
      Mpbs mpbs, TransactionDetails correspondingMobileTransaction, Instant toCompare) {
    Instant now = now();
    Fee fee = mpbs.getFee();
    MpbsVerification verifiedMobileTransaction =
        MpbsVerification.builder()
            .amountInPsp(correspondingMobileTransaction.getPspTransactionAmount())
            .fee(fee)
            .amountOfFeeRemainingPayment(fee.getRemainingAmount())
            .creationDatetimeOfMpbs(mpbs.getCreationDatetime())
            .creationDatetimeOfPaymentInPsp(
                correspondingMobileTransaction.getPspDatetimeTransactionCreation())
            .student(mpbs.getStudent())
            .build();

    // Update mpbs ...
    var mpbsNewStatus =
        defineMpbsStatusFromOrangeTransactionDetails(correspondingMobileTransaction);
    mpbs.setSuccessfullyVerifiedOn(now);
    mpbs.setStatus(mpbsNewStatus);
    mpbs.setPspOwnDatetimeVerification(
        correspondingMobileTransaction.getPspOwnDatetimeVerification());
    if (SUCCESS.equals(mpbsNewStatus)) {
      mpbs.setAmount(correspondingMobileTransaction.getPspTransactionAmount());
    }
    var successfullyVerifiedMpbs = mpbsService.save(mpbs);
    log.info("Mpbs has successfully verified = {}", mpbs);

    // ... then save the verification
    verifiedMobileTransaction.setMobileMoneyType(successfullyVerifiedMpbs.getMobileMoneyType());
    verifiedMobileTransaction.setPspId(successfullyVerifiedMpbs.getPspId());
    repository.save(verifiedMobileTransaction);

    // ... then save the corresponding payment
    paymentService.savePaymentFromMpbs(
        successfullyVerifiedMpbs, correspondingMobileTransaction.getPspTransactionAmount());
    log.info("Creating corresponding payment = {}", successfullyVerifiedMpbs.toString());

    // ... then update fee remaining amount
    feeService.debitAmount(fee, verifiedMobileTransaction.getAmountInPsp());

    return verifiedMobileTransaction;
  }

  private MpbsStatus defineMpbsStatusFromOrangeTransactionDetails(
      TransactionDetails storedTransaction) {

    // 1. if it contains and if the status is success then make it success
    if (SUCCESS.equals(storedTransaction.getStatus())) {
      log.info("correct");
      return SUCCESS;
    }
    // 2. and else if the mpbs is stored to day or less than 2 days, it will be verified later
    log.info("status computed = else");
    return PENDING;
  }

  private MpbsStatus defineMpbsStatusWithoutOrangeTransactionDetails(Mpbs mpbs, Instant toCompare) {
    long dayValidity = mpbs.getCreationDatetime().until(toCompare, ChronoUnit.DAYS);
    if (dayValidity > 2) {
      notifyStudentForFailedPayment(mpbs);
      return FAILED;
    }
    return PENDING;
  }

  private void notifyStudentForFailedPayment(Mpbs mpbs) {
    Fee correspondingFee = mpbs.getFee();
    Payment paymentFromMpbs =
        Payment.builder()
            .type(MOBILE_MONEY)
            .fee(correspondingFee)
            .amount(mpbs.getAmount())
            .creationDatetime(now())
            .comment(correspondingFee.getComment())
            .build();
    PaidFeeByMpbsFailedNotificationBody notificationBody =
        PaidFeeByMpbsFailedNotificationBody.from(paymentFromMpbs);
    eventProducer.accept(List.of(notificationBody));
    log.info(
        "Failed payment notification for user {} sent to Queue.",
        notificationBody.getMpbsAuthorEmail());
  }

  @Override
  public void accept(PendingMpbsCheckRequested pendingMpbsCheckRequested) {
    Mpbs mpbs = pendingMpbsCheckRequested.getToVerify();
    var verifyAt = pendingMpbsCheckRequested.getVerifyAt();
    log.info("Verifying {}", mpbs.getId());
    // Find transaction in database
    Optional<MobileTransactionDetails> mobileTransactionResponseDetails =
        mobilePaymentService.findTransactionByMpbsWithoutException(mpbs);

    // TIPS: do not use exception to continue script
    if (mobileTransactionResponseDetails.isPresent()) {
      TransactionDetails transactionDetails =
          transactionDetailsMapper.toExternalTransactionDetails(
              mobileTransactionResponseDetails.get());
      saveTheVerifiedMpbs(mpbs, transactionDetails, verifyAt);
      return;
    }
    saveTheUnverifiedMpbs(mpbs, verifyAt);
  }
}
