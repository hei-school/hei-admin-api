package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.InterestHistory;

import java.util.List;

@Repository
public interface InterestHistoryRepository extends JpaRepository<InterestHistory, String> {
    List<InterestHistory> getByFeeIdOrderByInterestStartDesc(String feeId);
}
