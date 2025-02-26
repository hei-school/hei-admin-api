package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.statistics.AdvancedFeeStats;

public interface AdvancesFeeStatsRepository extends JpaRepository<AdvancedFeeStats, String> {
  AdvancedFeeStats findFirstByOrderByInsertDatetimeDesc();
}
