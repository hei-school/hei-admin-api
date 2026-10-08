package school.hei.haapi.service.documenso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.DocumensoDocument;
import school.hei.haapi.model.DocumensoDocumentStatus;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.repository.DocumensoDocumentRepository;
import school.hei.haapi.repository.FileInfoRepository;

class DocumensoWebhookHandlerTest {
  private final DocumensoClient documensoClient = mock(DocumensoClient.class);
  private final DocumensoDocumentRepository documentRepository =
      mock(DocumensoDocumentRepository.class);
  private final FileInfoRepository fileInfoRepository = mock(FileInfoRepository.class);
  private final BucketComponent bucketComponent = mock(BucketComponent.class);

  private final DocumensoWebhookHandler subject =
      new DocumensoWebhookHandler(
          documensoClient, documentRepository, fileInfoRepository, bucketComponent);

  private static DocumensoWebhookPayload payload(String event, Long documentId) {
    return payload(event, documentId, null);
  }

  private static DocumensoWebhookPayload payload(
      String event, Long documentId, String completedAt) {
    return new DocumensoWebhookPayload(
        event, documentId == null ? null : new DocumensoDocumentEvent(documentId, completedAt));
  }

  @Test
  void an_event_other_than_completed_is_ignored() {
    subject.handle(payload("DOCUMENT_OPENED", 42L));
    verifyNoInteractions(documentRepository, documensoClient, bucketComponent);
  }

  @Test
  void an_event_without_payload_is_ignored() {
    subject.handle(payload("DOCUMENT_COMPLETED", null));
    verifyNoInteractions(documentRepository, documensoClient, bucketComponent);
  }

  @Test
  void an_event_without_id_is_ignored() {
    subject.handle(new DocumensoWebhookPayload("DOCUMENT_COMPLETED", new DocumensoDocumentEvent()));
    verifyNoInteractions(documentRepository, documensoClient, bucketComponent);
  }

  @Test
  void an_unknown_document_is_acknowledged_without_being_archived() {
    when(documentRepository.findByDocumensoDocumentId(42L)).thenReturn(Optional.empty());

    subject.handle(payload("DOCUMENT_COMPLETED", 42L));

    verify(bucketComponent, never()).upload(any(), any());
    verify(documentRepository, never()).save(any());
    verifyNoInteractions(documensoClient);
  }

  @Test
  void a_download_failure_surfaces_as_a_server_error() {
    when(documentRepository.findByDocumensoDocumentId(42L))
        .thenReturn(Optional.of(DocumensoDocument.builder().documensoDocumentId(42L).build()));
    when(documensoClient.downloadSignedDocument(42L)).thenThrow(new RestClientException("down"));

    assertThrows(ApiException.class, () -> subject.handle(payload("DOCUMENT_COMPLETED", 42L)));
    verify(documentRepository, never()).save(any());
  }

  @Test
  void a_completed_document_is_uploaded_and_marked_completed() throws Exception {
    var document = DocumensoDocument.builder().documensoDocumentId(42L).build();
    var signedFile = File.createTempFile("signed", ".pdf");
    when(documentRepository.findByDocumensoDocumentId(42L)).thenReturn(Optional.of(document));
    when(documensoClient.downloadSignedDocument(42L)).thenReturn(signedFile);
    when(fileInfoRepository.save(any())).thenAnswer(call -> call.getArgument(0, FileInfo.class));

    subject.handle(payload("DOCUMENT_COMPLETED", 42L, "2026-09-15T09:00:55.842Z"));

    var expectedKey =
        "DOCUMENSO/"
            + DateTimeFormatter.ofPattern("yyyy-MM")
                .withZone(ZoneOffset.UTC)
                .format(document.getArchivedDatetime())
            + "/42.pdf";
    verify(bucketComponent).upload(signedFile, expectedKey);
    verify(documentRepository).save(document);
    assertEquals(DocumensoDocumentStatus.COMPLETED, document.getStatus());
    assertEquals(expectedKey, document.getFileInfo().getFilePath());
    assertEquals(
        Instant.parse("2026-09-15T09:00:55.842Z"),
        document.getCompletedDatetime(),
        "the signature date is Documenso's, not ours");
  }

  @Test
  void the_completion_datetime_is_stamped() throws Exception {
    var document = DocumensoDocument.builder().documensoDocumentId(7L).build();
    when(documentRepository.findByDocumensoDocumentId(7L)).thenReturn(Optional.of(document));
    when(documensoClient.downloadSignedDocument(anyLong()))
        .thenReturn(File.createTempFile("signed", ".pdf"));
    when(fileInfoRepository.save(any())).thenAnswer(call -> call.getArgument(0, FileInfo.class));

    subject.handle(payload("DOCUMENT_COMPLETED", 7L));

    assertEquals(
        DocumensoDocumentStatus.COMPLETED,
        document.getStatus(),
        "status and completion datetime are set together");
    org.junit.jupiter.api.Assertions.assertNotNull(document.getCompletedDatetime());
  }

  @Test
  void a_date_documenso_did_not_give_or_we_cannot_read_counts_as_none() {
    assertNull(DocumensoWebhookHandler.parseDocumensoInstant(null));
    assertNull(DocumensoWebhookHandler.parseDocumensoInstant("   "));
    assertNull(DocumensoWebhookHandler.parseDocumensoInstant("15/09/2026"));
    assertEquals(
        Instant.parse("2026-09-15T09:00:55.842Z"),
        DocumensoWebhookHandler.parseDocumensoInstant("2026-09-15T09:00:55.842Z"));
  }

  @Test
  void an_unreadable_completion_date_still_archives_the_document() throws Exception {
    var document = DocumensoDocument.builder().documensoDocumentId(42L).build();
    var signedFile = File.createTempFile("signed", ".pdf");
    when(documentRepository.findByDocumensoDocumentId(42L)).thenReturn(Optional.of(document));
    when(documensoClient.downloadSignedDocument(42L)).thenReturn(signedFile);
    when(fileInfoRepository.save(any())).thenAnswer(call -> call.getArgument(0, FileInfo.class));

    subject.handle(payload("DOCUMENT_COMPLETED", 42L, "15/09/2026"));

    assertEquals(
        DocumensoDocumentStatus.COMPLETED,
        document.getStatus(),
        "a date we cannot read must not cost us a genuinely signed document");
    assertEquals(
        document.getArchivedDatetime(),
        document.getCompletedDatetime(),
        "with no readable signature date, the archiving date stands in");
  }
}
