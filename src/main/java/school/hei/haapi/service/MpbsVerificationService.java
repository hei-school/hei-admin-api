package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.http.model.MpReturnedType;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsVerification;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.repository.MpbsVerificationRepository;
import school.hei.haapi.repository.dao.MpbsDao;
import school.hei.haapi.service.orange.OrangeApi;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

@Service
@AllArgsConstructor
@Slf4j
public class MpbsVerificationService {
  private final MpbsVerificationRepository repository;
  private final MpbsDao mpbsDao;
  private final OrangeApi orangeApi;

  public List<MpbsVerification> findAllByStudentIdAndFeeId(String studentId, String feeId) {
    return repository.findAllByStudentIdAndFeeId(studentId, feeId);
  }

  public MpbsVerification isMobilePaymentThenSaveVerification(Mpbs mpbs) {
    try {
      MpReturnedType orangeMobilePaymentData = orangeApi.checkTransactionByRef(mpbs.getPspId());
      Fee fee = mpbs.getFee();
      MpbsVerification verifiedPayment = MpbsVerification.builder()
              .amountInPsp(orangeMobilePaymentData.getTransactionAmount())
              .fee(fee)
              .amountOfFeeRemainingPayment(fee.getRemainingAmount())
              .creationDatetimeOfMpbs(mpbs.getCreationDatetime())
              .creationDatetimeOfPaymentInPsp(orangeMobilePaymentData.getTransactionCreation())
              .student(mpbs.getStudent())
              .build();

      log.info("Mpbs has successfully verified = {}", mpbs);
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
