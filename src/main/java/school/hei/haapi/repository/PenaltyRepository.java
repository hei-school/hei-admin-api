package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Penalty;
@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, String> {
}
