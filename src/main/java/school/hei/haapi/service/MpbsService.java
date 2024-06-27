package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.MpbsRepository;

@Service
@AllArgsConstructor
public class MpbsService {
  private final MpbsRepository mpbsRepository;

  public Mpbs saveMpbs(Mpbs mobilePaymentByStudentToSave) {
    return mpbsRepository.save(mobilePaymentByStudentToSave);
  }

  public Mpbs getStudentMobilePaymentByFeeId(String studentId, String feeId) {
    return mpbsRepository.findByStudentIdAndFeeId(studentId, feeId);
  }

  public Mpbs getByPspId(String pspId) {
    return mpbsRepository
        .findByPspId(pspId)
        .orElseThrow(() -> new NotFoundException("Mpbs not found #" + pspId));
  }
}
