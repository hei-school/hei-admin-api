package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Fee;

@Repository
public interface FeeRepository extends JpaRepository<Fee, String> {
  Fee getByUserIdAndId(String userId, String feeId);
}
