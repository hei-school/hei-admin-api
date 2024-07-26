package school.hei.haapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.WorkDocument;

@Repository
public interface WorkDocumentRepository extends JpaRepository<WorkDocument, String> {
  Optional<WorkDocument> findTopByStudentIdOrderByCreationDatetimeDesc(String studentId);
}
