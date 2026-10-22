package school.hei.haapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.ComplaintRouting;
import school.hei.haapi.model.ComplaintType;

@Repository
public interface ComplaintRoutingRepository extends JpaRepository<ComplaintRouting, String> {
  Optional<ComplaintRouting> findFirstByTypeOrderByCreationDatetimeDesc(ComplaintType type);
}
