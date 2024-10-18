package school.hei.haapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.FeeTemplate;

@Repository
public interface FeeTemplateRepository extends JpaRepository<FeeTemplate, String> {
  Optional<FeeTemplate> findByName(String name);
}
