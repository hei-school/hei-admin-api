package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsVerification;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.repository.MpbsVerificationRepository;

@Component
@AllArgsConstructor
@Slf4j
public class ComputeVerifiedMobilePayement {
  private final MpbsVerificationRepository mpbsVerificationRepository;
  private final MpbsRepository mpbsRepository;
  private final FeeService feeService;
  private final PaymentService paymentService;

  public MpbsVerification saveTheVerifiedMpbs(
      Mpbs mpbs, TransactionDetails correspondingMobileTransaction) {
    Instant now = Instant.now();
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
    mpbs.setSuccessfullyVerifiedOn(now);
    mpbs.setStatus(defineMpbsStatusFromOrangeTransactionDetails(correspondingMobileTransaction));
    mpbs.setPspOwnDatetimeVerification(
        correspondingMobileTransaction.getPspOwnDatetimeVerification());
    var successfullyVerifiedMpbs = mpbsRepository.save(mpbs);

    // ... then save the verification
    verifiedMobileTransaction.setMobileMoneyType(successfullyVerifiedMpbs.getMobileMoneyType());
    verifiedMobileTransaction.setPspId(successfullyVerifiedMpbs.getPspId());
    mpbsVerificationRepository.save(verifiedMobileTransaction);

    // ... then save the corresponding payment
    paymentService.savePaymentFromMpbs(
        successfullyVerifiedMpbs, correspondingMobileTransaction.getPspTransactionAmount());

    // ... then update student status
    paymentService.computeUserStatusAfterPayingFee(mpbs.getStudent());

    // ... then update fee remaining amount
    feeService.debitAmountFromMpbs(fee, verifiedMobileTransaction.getAmountInPsp());

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
}
