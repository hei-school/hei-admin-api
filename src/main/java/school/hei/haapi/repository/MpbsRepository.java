package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.Mpbs.Mpbs;

@Repository
public interface MpbsRepository extends JpaRepository<Mpbs, String> {
  List<Mpbs> findByStudentIdAndFeeId(String studentId, String feeId);

  Optional<Mpbs> findByPspId(String pspId);

  List<Mpbs> findByPspIdIn(List<String> pspId);

  List<Mpbs> findAllByStatus(MpbsStatus status);

  Long countMpbsByStatusAndStudentId(MpbsStatus status, String studentId);
}
