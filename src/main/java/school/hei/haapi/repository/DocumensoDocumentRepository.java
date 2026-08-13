package school.hei.haapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.DocumensoDocument;

@Repository
public interface DocumensoDocumentRepository extends JpaRepository<DocumensoDocument, String> {
  Optional<DocumensoDocument> findByDocumensoDocumentId(Long documensoDocumentId);
}
