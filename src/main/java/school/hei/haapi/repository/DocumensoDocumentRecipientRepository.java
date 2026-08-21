package school.hei.haapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.DocumensoDocumentRecipient;

@Repository
public interface DocumensoDocumentRecipientRepository
    extends JpaRepository<DocumensoDocumentRecipient, String> {
  Optional<DocumensoDocumentRecipient> findByDocument_IdAndUser_Id(
      String documentId, String userId);
}
