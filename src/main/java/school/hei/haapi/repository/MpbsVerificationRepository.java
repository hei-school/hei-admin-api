package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Mpbs.MpbsVerification;

@Repository
public interface MpbsVerificationRepository extends JpaRepository<MpbsVerification, String> {
  List<MpbsVerification> findAllByStudentIdAndFeeId(String studentId, String feeId);
}
