package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Mpbs.MpbsVerification;
import school.hei.haapi.repository.MpbsVerificationRepository;

@Service
@AllArgsConstructor
public class MpbsVerificationService {
  private final MpbsVerificationRepository repository;

  public List<MpbsVerification> findAllByStudentIdAndFeeId(String studentId, String feeId) {
    return repository.findAllByStudentIdAndFeeId(studentId, feeId);
  }
}
