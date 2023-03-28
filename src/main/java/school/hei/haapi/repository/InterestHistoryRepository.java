package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.InterestHistory;

@Repository
public interface InterestHistoryRepository extends JpaRepository<InterestHistory, String> {
}
