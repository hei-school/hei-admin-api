package school.hei.haapi.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.DocumensoDocument;
import school.hei.haapi.model.DocumensoDocumentStatus;

@Repository
public interface DocumensoDocumentRepository extends JpaRepository<DocumensoDocument, String> {
  Optional<DocumensoDocument> findByDocumensoDocumentId(Long documensoDocumentId);

  List<DocumensoDocument> findAllByStatus(DocumensoDocumentStatus status);

  Optional<DocumensoDocument> findFirstBySubject_IdAndTemplate_IdAndStatusIn(
      String subjectId, String templateId, Collection<DocumensoDocumentStatus> statuses);

  @Query(
      """
      SELECT d FROM DocumensoDocument d
      WHERE d.subject IN (SELECT s FROM User m JOIN m.monitors s WHERE m.id = :monitorId)
      ORDER BY d.creationDatetime DESC
      """)
  List<DocumensoDocument> findAllByMonitorId(
      @Param("monitorId") String monitorId, Pageable pageable);
}
