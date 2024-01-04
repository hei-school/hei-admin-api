package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.FeeType;

@Repository
public interface FeeTypeRepository extends JpaRepository<FeeType, String> {
}
