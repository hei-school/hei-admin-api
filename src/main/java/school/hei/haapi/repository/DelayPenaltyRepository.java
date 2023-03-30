package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.DelayPenalty;

import java.util.List;

@Repository
public interface DelayPenaltyRepository extends JpaRepository<DelayPenalty, String> {
    List<DelayPenalty> findFirstByOrderByCreationDatetimeDesc();
}
