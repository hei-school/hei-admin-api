package school.hei.haapi.service.event;

import java.time.Instant;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.PendingDocumensoDocumentsCheckTriggered;
import school.hei.haapi.model.DocumensoDocument;
import school.hei.haapi.model.DocumensoDocumentStatus;
import school.hei.haapi.repository.DocumensoDocumentRepository;
import school.hei.haapi.service.documenso.DocumensoClient;
import school.hei.haapi.service.documenso.DocumensoWebhookHandler;

@Slf4j
@Service
@AllArgsConstructor
public class PendingDocumensoDocumentsCheckTriggeredService
    implements Consumer<PendingDocumensoDocumentsCheckTriggered> {
  private final DocumensoDocumentRepository documensoDocumentRepository;
  private final DocumensoClient documensoClient;
  private final DocumensoWebhookHandler webhookHandler;

  @Override
  public void accept(PendingDocumensoDocumentsCheckTriggered event) {
    var pending = documensoDocumentRepository.findAllByStatus(DocumensoDocumentStatus.PENDING);
    if (pending.isEmpty()) {
      return;
    }

    var repaired = 0;
    for (var document : pending) {
      try {
        if (reconcile(document)) {
          repaired++;
        }
      } catch (Exception e) {
        // one unreachable document must not end the sweep for the others
        log.warn(
            "Could not reconcile Documenso document id={}: {}",
            document.getDocumensoDocumentId(),
            e.getMessage());
      }
    }
    log.info("Documenso sweep: {} of {} pending documents were behind", repaired, pending.size());
  }

  private boolean reconcile(DocumensoDocument document) {
    var remote = documensoClient.getDocument(document.getDocumensoDocumentId());
    if (remote.getStatus() == null) {
      return false;
    }
    return switch (remote.getStatus()) {
      case COMPLETED -> {
        webhookHandler.archiveSignedDocument(document);
        yield true;
      }
      case REJECTED -> {
        markRejected(document);
        yield true;
      }
      case DRAFT, PENDING -> false;
    };
  }

  private void markRejected(DocumensoDocument document) {
    document.setStatus(DocumensoDocumentStatus.REJECTED);
    document.setCompletedDatetime(Instant.now());
    documensoDocumentRepository.save(document);
  }
}
