package school.hei.haapi.service;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsVerification;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.repository.MpbsVerificationRepository;
import school.hei.haapi.repository.dao.MpbsDao;

@Service
@AllArgsConstructor
@Slf4j
public class MpbsVerificationService {
  private final MpbsVerificationRepository repository;
  private final MpbsDao mpbsDao;
  private final MpbsRepository mpbsRepository;
  private final MobilePaymentService mobilePaymentService;

  public List<MpbsVerification> findAllByStudentIdAndFeeId(String studentId, String feeId) {
    return repository.findAllByStudentIdAndFeeId(studentId, feeId);
  }

  public MpbsVerification isMobilePaymentThenSaveVerification(Mpbs mpbs) {
    try {
      var transactionDetails = mobilePaymentService.findTransactionByMpbs(mpbs);
      Fee fee = mpbs.getFee();
      MpbsVerification verifiedPayment =
          MpbsVerification.builder()
              .amountInPsp(transactionDetails.getPspTransactionAmount())
              .fee(fee)
              .amountOfFeeRemainingPayment(fee.getRemainingAmount())
              .creationDatetimeOfMpbs(mpbs.getCreationDatetime())
              .creationDatetimeOfPaymentInPsp(
                  transactionDetails.getPspDatetimeTransactionCreation())
              .student(mpbs.getStudent())
              .build();

      log.info("Mpbs has successfully verified = {}", mpbs);
      mpbs.setSuccessfullyVerifiedOn(Instant.now());
      mpbsRepository.save(mpbs);
      return repository.save(verifiedPayment);
    } catch (ApiException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  public void checkMobilePaymentThenSaveVerification() {
    List<Mpbs> mpbsOfTheWeek = mpbsDao.findMpbsOfTheWeek();
    mpbsOfTheWeek.forEach(this::isMobilePaymentThenSaveVerification);
  }
}
