package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.GradeChangeHistory;

public interface GradeChangeHistoryRepository extends JpaRepository<GradeChangeHistory, String> {}
