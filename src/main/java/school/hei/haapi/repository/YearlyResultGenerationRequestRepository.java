package school.hei.haapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.YearlyResultGenerationRequest;

@Repository
public interface YearlyResultGenerationRequestRepository
    extends JpaRepository<YearlyResultGenerationRequest, String> {
  Optional<YearlyResultGenerationRequest>
      findFirstYearlyResultGenerationRequestByFileNameContainsIgnoreCaseOrderByFileNameDesc(
          String fileName);
}
