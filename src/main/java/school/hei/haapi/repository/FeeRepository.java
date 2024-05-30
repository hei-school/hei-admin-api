package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.model.Fee;

@Repository
public interface FeeRepository extends JpaRepository<Fee, String> {
  Fee getByStudentIdAndId(String studentId, String feeId);

  List<Fee> getFeesByStatus(FeeStatusEnum status, Pageable pageable);

  List<Fee> findAllByStatus(FeeStatusEnum status);

  List<Fee> getFeesByStudentIdAndStatus(String studentId, FeeStatusEnum status, Pageable pageable);

  List<Fee> getByStudentId(String studentId, Pageable pageable);

  @Query(
      "select f from Fee f where f.status = 'UNPAID' "
          + "and f.remainingAmount > 0 "
          + "and f.dueDatetime < :now")
  List<Fee> getUnpaidFees(@Param(value = "now") Instant now);

  @Modifying
  @Query("update Fee f set f.status = :status " + "where f.id = :fee_id")
  void updateFeeStatusById(
      @Param(value = "status") FeeStatusEnum status, @Param(value = "fee_id") String feeId);
}
