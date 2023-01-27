package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.Fee.StatusEnum;
import school.hei.haapi.model.Fee;
import java.util.List;

@Repository
public interface FeeRepository extends JpaRepository<Fee, String> {
  Fee getByStudentIdAndId(String studentId, String feeId);

  List<Fee> getFeesByStatus(StatusEnum status, Pageable pageable);

  List<Fee> getFeesByStudentIdAndStatus(String studentId, StatusEnum status, Pageable pageable);

  List<Fee> getByStudentId(String studentId, Pageable pageable);
}
