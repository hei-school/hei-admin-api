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

/**
 * Reconciles documents still pending with what Documenso actually holds.
 *
 * <p>The webhook is a single delivery over the network: one missed call and a signed document stays
 * pending forever, invisible to the monitor and blocking any new request for it. This sweep asks
 * Documenso directly, so that state repairs itself on the next run.
 */
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

  /** Returns true when the document was behind and has just been brought up to date. */
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
      // a rejection never reaches us by webhook, and leaving it pending would bar the student
      // from ever being asked again
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
