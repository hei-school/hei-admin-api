package school.hei.haapi.service;

import static java.util.stream.Collectors.toList;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsVerification;
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

  @Transactional
  public MpbsVerification verifyMobilePaymentAndSaveResult(Mpbs mpbs, Instant toCompare) {
    log.info("Magic happened here");
    // Find transaction in database
    TransactionDetails mobileTransactionResponseDetails =
        mobilePaymentService.findTransactionByMpbsWithoutException(mpbs);

    // TIPS: do not use exception to continue script
    if (mobileTransactionResponseDetails != null) {
      saveTheVerifiedMpbs(mpbs, mobileTransactionResponseDetails, toCompare);
    }
    saveTheUnverifiedMpbs(mpbs, mobileTransactionResponseDetails, toCompare);
    return null;
  }

  private Mpbs saveTheUnverifiedMpbs(
      Mpbs mpbs, TransactionDetails transactionDetails, Instant toCompare) {
    mpbs.setStatus(
        defineMpbsStatusByOrangeStatusOrByInstantValidity(transactionDetails, mpbs, toCompare));
    return mpbsRepository.save(mpbs);
  }

  private MpbsVerification saveTheVerifiedMpbs(
      Mpbs mpbs, TransactionDetails correspondingMobileTransaction, Instant toCompare) {
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
    mpbs.setSuccessfullyVerifiedOn(Instant.now());
    mpbs.setStatus(
        defineMpbsStatusByOrangeStatusOrByInstantValidity(
            correspondingMobileTransaction, mpbs, toCompare));
    var successfullyVerifiedMpbs = mpbsRepository.save(mpbs);
    log.info("Mpbs has successfully verified = {}", mpbs);

    // ... then save the verification
    verifiedMobileTransaction.setMobileMoneyType(successfullyVerifiedMpbs.getMobileMoneyType());
    verifiedMobileTransaction.setPspId(successfullyVerifiedMpbs.getPspId());
    repository.save(verifiedMobileTransaction);

    // ... then update fee remaining amount
    feeService.debitAmount(fee, verifiedMobileTransaction.getAmountInPsp());
    return verifiedMobileTransaction;
  }

  public List<MpbsVerification> checkMobilePaymentThenSaveVerification() {
    List<Mpbs> pendingMpbs = mpbsRepository.findAllByStatus(PENDING);
    log.info("pending mpbs = {}", pendingMpbs.size());
    Instant now = Instant.now();

    return pendingMpbs.stream()
        .map((mpbs -> this.verifyMobilePaymentAndSaveResult(mpbs, now)))
        .collect(toList());
  }

  public List<TransactionDetails> fetchThenSaveTransactionDetailsDaily() {
    return mobilePaymentService.fetchThenSaveTransactionDetails();
  }

  private MpbsStatus defineMpbsStatusByOrangeStatusOrByInstantValidity(
      TransactionDetails storedTransaction, Mpbs mpbs, Instant toCompare) {
    long dayValidity = mpbs.getCreationDatetime().until(toCompare, ChronoUnit.DAYS);

    if (storedTransaction == null) {
      if (dayValidity > 2) {
        return FAILED;
      }
    }
    if (dayValidity > 2) {
      return FAILED;
    }
    if (SUCCESS.equals(storedTransaction.getStatus())) {
      return SUCCESS;
    }

    return PENDING;
  }
}
