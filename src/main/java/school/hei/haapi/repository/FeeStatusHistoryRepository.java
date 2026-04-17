package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeStatusHistory;
import school.hei.haapi.model.mpbs.MpbsStatusHistory;

@Repository
public interface FeeStatusHistoryRepository extends JpaRepository<FeeStatusHistory, String> {
  List<FeeStatusHistory> findByFeeIdOrderByDatetime(String feeId, Sort sort);

  List<FeeStatusHistory> findByFeeId(String feeId);

  Long fee(Fee fee);
}
