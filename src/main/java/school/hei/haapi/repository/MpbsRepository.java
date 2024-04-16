package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Mpbs.Mpbs;

@Repository
public interface MpbsRepository extends JpaRepository<Mpbs, String> {
  Mpbs findByStudentIdAndFeeId(String studentId, String FeeId);
}
