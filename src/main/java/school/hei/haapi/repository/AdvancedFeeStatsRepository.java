package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.statistics.AdvancedFeeStats;

public interface AdvancedFeeStatsRepository extends JpaRepository<AdvancedFeeStats, String> {}
