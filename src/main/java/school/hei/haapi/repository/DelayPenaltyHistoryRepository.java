package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.DelayPenaltyHistory;

@Repository
public interface DelayPenaltyHistoryRepository extends JpaRepository<DelayPenaltyHistory, String> {
}
