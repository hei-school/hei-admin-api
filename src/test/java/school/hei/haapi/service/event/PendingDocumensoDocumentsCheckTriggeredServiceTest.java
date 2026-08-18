package school.hei.haapi.service.event;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static school.hei.haapi.model.DocumensoDocumentStatus.PENDING;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import school.hei.haapi.endpoint.event.model.PendingDocumensoDocumentsCheckTriggered;
import school.hei.haapi.model.DocumensoDocument;
import school.hei.haapi.model.DocumensoDocumentStatus;
import school.hei.haapi.repository.DocumensoDocumentRepository;
import school.hei.haapi.service.documenso.DocumensoClient;
import school.hei.haapi.service.documenso.DocumensoWebhookHandler;
import school.hei.haapi.service.documenso.gen.model.DocumentGet200Response;

class PendingDocumensoDocumentsCheckTriggeredServiceTest {
  private final DocumensoDocumentRepository documentRepository =
      mock(DocumensoDocumentRepository.class);
  private final DocumensoClient documensoClient = mock(DocumensoClient.class);
  private final DocumensoWebhookHandler webhookHandler = mock(DocumensoWebhookHandler.class);

  private final PendingDocumensoDocumentsCheckTriggeredService subject =
      new PendingDocumensoDocumentsCheckTriggeredService(
          documentRepository, documensoClient, webhookHandler);

  private static DocumensoDocument aPendingDocument(long documensoId) {
    return DocumensoDocument.builder()
        .id(randomUUID().toString())
        .documensoDocumentId(documensoId)
        .status(PENDING)
        .build();
  }

  private void givenPending(DocumensoDocument... documents) {
    when(documentRepository.findAllByStatus(PENDING)).thenReturn(List.of(documents));
  }

  private void givenRemoteStatus(long documensoId, DocumentGet200Response.StatusEnum status) {
    when(documensoClient.getDocument(documensoId))
        .thenReturn(new DocumentGet200Response().status(status));
  }

  @Test
  void nothing_pending_means_nothing_asked_of_documenso() {
    givenPending();

    subject.accept(new PendingDocumensoDocumentsCheckTriggered());

    verifyNoInteractions(documensoClient, webhookHandler);
  }

  @Test
  void a_document_signed_behind_our_back_is_archived() {
    var document = aPendingDocument(42L);
    givenPending(document);
    givenRemoteStatus(42L, DocumentGet200Response.StatusEnum.COMPLETED);

    subject.accept(new PendingDocumensoDocumentsCheckTriggered());

    verify(webhookHandler).archiveSignedDocument(document);
  }

  @Test
  void a_document_still_pending_at_documenso_is_left_alone() {
    var document = aPendingDocument(42L);
    givenPending(document);
    givenRemoteStatus(42L, DocumentGet200Response.StatusEnum.PENDING);

    subject.accept(new PendingDocumensoDocumentsCheckTriggered());

    verify(webhookHandler, never()).archiveSignedDocument(any());
    verify(documentRepository, never()).save(any());
  }

  @Test
  void a_rejected_document_stops_blocking_a_new_request() {
    var document = aPendingDocument(42L);
    givenPending(document);
    givenRemoteStatus(42L, DocumentGet200Response.StatusEnum.REJECTED);

    subject.accept(new PendingDocumensoDocumentsCheckTriggered());

    assertEquals(DocumensoDocumentStatus.REJECTED, document.getStatus());
    assertNotNull(document.getCompletedDatetime());
    verify(documentRepository).save(document);
    verify(webhookHandler, never()).archiveSignedDocument(any());
  }

  @Test
  void one_unreachable_document_does_not_end_the_sweep() {
    var unreachable = aPendingDocument(42L);
    var signed = aPendingDocument(43L);
    givenPending(unreachable, signed);
    when(documensoClient.getDocument(42L)).thenThrow(new RestClientException("down"));
    givenRemoteStatus(43L, DocumentGet200Response.StatusEnum.COMPLETED);

    assertDoesNotThrow(() -> subject.accept(new PendingDocumensoDocumentsCheckTriggered()));

    verify(webhookHandler).archiveSignedDocument(signed);
  }

  @Test
  void a_remote_document_without_status_is_left_alone() {
    var document = aPendingDocument(42L);
    givenPending(document);
    when(documensoClient.getDocument(anyLong())).thenReturn(new DocumentGet200Response());

    subject.accept(new PendingDocumensoDocumentsCheckTriggered());

    verify(webhookHandler, never()).archiveSignedDocument(any());
    verify(documentRepository, never()).save(any());
  }
}
