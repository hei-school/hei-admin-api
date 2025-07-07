package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsVerification;
import school.hei.haapi.repository.MpbsVerificationRepository;

@Component
@AllArgsConstructor
@Slf4j
public class ComputeVerifiedMobilePayment {
  private final MpbsVerificationRepository mpbsVerificationRepository;
  private final MpbsService mpbsService;
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
    mpbs.setStatus(SUCCESS);
    mpbs.setPspOwnDatetimeVerification(
        correspondingMobileTransaction.getPspOwnDatetimeVerification());
    var successfullyVerifiedMpbs = mpbsService.save(mpbs);

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
}
