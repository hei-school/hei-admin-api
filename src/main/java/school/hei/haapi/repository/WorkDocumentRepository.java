package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.WorkDocument;

@Repository
public interface WorkDocumentRepository extends JpaRepository<WorkDocument, String> {
  List<WorkDocument> findAllByStudentId(String studentId, Pageable pageable);
}
