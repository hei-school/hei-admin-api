package school.hei.haapi.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.mpbs.Mpbs;

@Repository
public interface MpbsRepository extends JpaRepository<Mpbs, String> {
  List<Mpbs> findByStudentIdAndFeeId(String studentId, String feeId);

  Optional<Mpbs> findByPspId(String pspId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select m from Mpbs m where m.id = :id")
  Optional<Mpbs> findByIdForUpdate(@Param("id") String id);

  List<Mpbs> findByPspIdIn(List<String> pspId);

  List<Mpbs> findAllByStatus(MpbsStatus status);

  Long countMpbsByStatusAndStudentId(MpbsStatus status, String studentId);

  List<Mpbs> findAllByStatusAndStudentId(MpbsStatus status, String studentId);
}
