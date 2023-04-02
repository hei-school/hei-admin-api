package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import school.hei.haapi.model.DelayPenalty;

public interface DelayRepository extends JpaRepository<DelayPenalty, String> {
    @Query("select d from DelayPenalty d")
    DelayPenalty get();
}
