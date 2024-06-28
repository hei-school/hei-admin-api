package school.hei.haapi.service;

import static java.util.stream.Collectors.toList;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsVerification;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.repository.MpbsVerificationRepository;

@Service
@AllArgsConstructor
@Slf4j
public class MpbsVerificationService {
  private final MpbsVerificationRepository repository;
  private final MpbsRepository mpbsRepository;
  private final FeeService feeService;
  private final MobilePaymentService mobilePaymentService;

  public List<MpbsVerification> findAllByStudentIdAndFeeId(String studentId, String feeId) {
    return repository.findAllByStudentIdAndFeeId(studentId, feeId);
  }

  public MpbsVerification verifyMobilePaymentAndSaveResult(Mpbs mpbs) {
    try {
      // Find transaction in database
      TransactionDetails mobileTransactionResponseDetails =
          mobilePaymentService.findTransactionByMpbs(mpbs);
      Fee fee = mpbs.getFee();
      MpbsVerification verifiedMobileTransaction =
          MpbsVerification.builder()
              .amountInPsp(mobileTransactionResponseDetails.getPspTransactionAmount())
              .fee(fee)
              .amountOfFeeRemainingPayment(fee.getRemainingAmount())
              .creationDatetimeOfMpbs(mpbs.getCreationDatetime())
              .creationDatetimeOfPaymentInPsp(
                  mobileTransactionResponseDetails.getPspDatetimeTransactionCreation())
              .student(mpbs.getStudent())
              .build();

      // Update mpbs ...
      mpbs.setSuccessfullyVerifiedOn(Instant.now());
      mpbs.setStatus(mobileTransactionResponseDetails.getStatus());
      var successfullyVerifiedMpbs = mpbsRepository.save(mpbs);
      log.info("Mpbs has successfully verified = {}", mpbs);

      // ... then save the verification
      verifiedMobileTransaction.setMobileMoneyType(successfullyVerifiedMpbs.getMobileMoneyType());
      verifiedMobileTransaction.setPspId(successfullyVerifiedMpbs.getPspId());
      repository.save(verifiedMobileTransaction);

      // ... then update fee remaining amount
      feeService.debitAmount(fee, verifiedMobileTransaction.getAmountInPsp());
      return verifiedMobileTransaction;
    } catch (ApiException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  public List<MpbsVerification> checkMobilePaymentThenSaveVerification() {
    List<Mpbs> pendingMpbs = mpbsRepository.findAllByStatus(PENDING);

    return pendingMpbs.stream().map(this::verifyMobilePaymentAndSaveResult).collect(toList());
  }

  public List<TransactionDetails> fetchThenSaveTransactionDetailsDaily() {
    return mobilePaymentService.fetchThenSaveTransactionDetails();
  }
}
