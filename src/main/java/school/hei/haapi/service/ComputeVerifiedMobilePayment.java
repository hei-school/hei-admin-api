package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;

import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsVerification;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.repository.MpbsVerificationRepository;

@Component
@AllArgsConstructor
@Slf4j
public class ComputeVerifiedMobilePayment {
  private final MpbsVerificationRepository mpbsVerificationRepository;
  private final MpbsRepository mpbsRepository;
  private final MpbsService mpbsService;
  private final FeeService feeService;
  private final PaymentService paymentService;

  @Transactional
  public MpbsVerification saveTheVerifiedMpbs(
      Mpbs mpbs, TransactionDetails correspondingMobileTransaction) {
    if (!SUCCESS.equals(correspondingMobileTransaction.getStatus()))
      throw new BadRequestException(
          "Corresponding mobile payment transaction details must be successful for the payment %s"
              .formatted(mpbs.getId()));

    var lockedMpbs =
        mpbsRepository
            .findByIdForUpdate(mpbs.getId())
            .orElseThrow(() -> new NotFoundException("Mpbs not found #" + mpbs.getId()));
    if (paymentService.hasPaymentFromMpbs(lockedMpbs.getId())) {
      log.info(
          "Mpbs {} was already verified and paid while waiting for the lock, skipping",
          lockedMpbs.getId());
      return null;
    }

    Instant now = Instant.now();
    Fee fee = lockedMpbs.getFee();
    MpbsVerification verifiedMobileTransaction =
        MpbsVerification.builder()
            .amountInPsp(correspondingMobileTransaction.getPspTransactionAmount())
            .fee(fee)
            .amountOfFeeRemainingPayment(fee.getRemainingAmount())
            .creationDatetimeOfMpbs(lockedMpbs.getCreationDatetime())
            .creationDatetimeOfPaymentInPsp(
                correspondingMobileTransaction.getPspDatetimeTransactionCreation())
            .student(lockedMpbs.getStudent())
            .build();

    // Update mpbs ...
    lockedMpbs.setSuccessfullyVerifiedOn(now);
    lockedMpbs.setStatus(SUCCESS);
    lockedMpbs.setPspOwnDatetimeVerification(
        correspondingMobileTransaction.getPspOwnDatetimeVerification());
    lockedMpbs.setAmount(correspondingMobileTransaction.getPspTransactionAmount());
    var successfullyVerifiedMpbs = mpbsService.save(lockedMpbs);

    // ... then save the verification
    verifiedMobileTransaction.setMobileMoneyType(successfullyVerifiedMpbs.getMobileMoneyType());
    verifiedMobileTransaction.setPspId(successfullyVerifiedMpbs.getPspId());
    mpbsVerificationRepository.save(verifiedMobileTransaction);

    var unreconciledPayment = paymentService.findUnreconciledPaymentByFeeId(fee.getId());
    if (unreconciledPayment.isPresent()) {
      log.info(
          "Fee {} already has a manually recorded payment {}, reconciling it with mpbs {} instead"
              + " of debiting the fee again",
          fee.getId(),
          unreconciledPayment.get().getId(),
          lockedMpbs.getId());
      paymentService.reconcilePaymentWithMpbs(unreconciledPayment.get(), successfullyVerifiedMpbs);
      return verifiedMobileTransaction;
    }

    // ... then update fee remaining amount
    feeService.debitAmountFromMpbs(fee, verifiedMobileTransaction.getAmountInPsp());

    // ... then save the corresponding payment
    paymentService.savePaymentFromMpbs(
        successfullyVerifiedMpbs, correspondingMobileTransaction.getPspTransactionAmount());

    // ... then update student status
    feeService.computeUserStatusAfterPayingFee(lockedMpbs.getStudent());

    return verifiedMobileTransaction;
  }
}
