package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.FeeTypeComponentEntity;

@Repository
public interface FeeTypeComponentRepository extends JpaRepository<FeeTypeComponentEntity, String> {
  List<FeeTypeComponentEntity> getByFeeTypeEntityId(String typeFeeId);
}
