package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum;
import school.hei.haapi.model.WorkDocument;

@Repository
public interface WorkDocumentRepository extends JpaRepository<WorkDocument, String> {
  Optional<WorkDocument> findTopByStudentIdOrderByCreationDatetimeDesc(String studentId);
}
