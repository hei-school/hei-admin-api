package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Fee;

@Repository
public interface DelayPenaltyRepository extends JpaRepository<DelayPenalty, String>{
}
