package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.Penalty;

public interface PenaltyRepository extends JpaRepository<Penalty, String> {
}
