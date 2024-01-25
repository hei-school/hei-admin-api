package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.FeeTemplate;

import java.util.Optional;

@Repository
public interface FeeTemplateRepository extends JpaRepository<FeeTemplate, String> {}
