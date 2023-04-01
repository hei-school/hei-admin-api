package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.DelayPenalty;

@Repository
public interface DelayPenaltyRepository extends JpaRepository<DelayPenalty, String> {
    @Query("select count(d) from DelayPenalty d")
    int countPenaltyConfigs();
}
