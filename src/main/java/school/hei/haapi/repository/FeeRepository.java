package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.Fee.StatusEnum;
import school.hei.haapi.model.Fee;

@Repository
public interface FeeRepository extends JpaRepository<Fee, String> {
  Fee getByStudentIdAndId(String studentId, String feeId);

  List<Fee> getFeesByStatus(StatusEnum status,
                            Pageable pageable);

  List<Fee> getFeesByStatusAndStudentId(StatusEnum status,
                                        String studentId);

  List<Fee> getFeesByStatus(StatusEnum status);

  List<Fee> getFeesByStudentIdAndStatus(String studentId,
                                        StatusEnum status,
                                        Pageable pageable);

  List<Fee> getByStudentId(String studentId, Pageable pageable);

  @Query("select f from Fee f where f.status = 'UNPAID' "
      + "and f.remainingAmount > 0 "
      + "and f.dueDatetime < current_date")
  List<Fee> getUnpaidFees();
}