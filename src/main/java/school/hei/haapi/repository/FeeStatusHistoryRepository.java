package school.hei.haapi.repository;

import jakarta.persistence.criteria.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeStatusHistory;

import java.time.Instant;
import java.util.List;

@Repository
public interface FeeStatusHistoryRepository extends JpaRepository<FeeStatusHistory, String> {
  List<FeeStatusHistory> findByFeeIdOrderByDatetime(String feeId, Sort sort);

  Long fee(Fee fee);
}
