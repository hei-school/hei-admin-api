package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.DelayPenalty;

import java.time.Instant;

@Repository
public interface DelayPenaltyRepository extends JpaRepository<DelayPenalty, String> {
    DelayPenalty findOneOrderByCreationDatetimeDesc();
}
