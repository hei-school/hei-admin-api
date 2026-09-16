package school.hei.haapi.service.documenso;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.DocumensoDocument;
import school.hei.haapi.model.DocumensoDocumentStatus;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.ApiException.ExceptionType;
import school.hei.haapi.repository.DocumensoDocumentRepository;
import school.hei.haapi.repository.FileInfoRepository;

@Component
@AllArgsConstructor
@Slf4j
public class DocumensoWebhookHandler {
  private static final String BUCKET_FOLDER = "DOCUMENSO";
  private static final DateTimeFormatter MONTH_FOLDER =
      DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneOffset.UTC);

  private final DocumensoClient documensoClient;
  private final DocumensoDocumentRepository documensoDocumentRepository;
  private final FileInfoRepository fileInfoRepository;
  private final BucketComponent bucketComponent;

  @Transactional
  public void handle(DocumensoWebhookPayload payload) {
    if (!payload.isDocumentCompleted()) {
      return;
    }
    if (payload.getPayload() == null || payload.getPayload().getId() == null) {
      return;
    }
    var documensoDocumentId = payload.getPayload().getId();
    var document = documensoDocumentRepository.findByDocumensoDocumentId(documensoDocumentId);
    if (document.isEmpty()) {
      log.warn("Ignoring webhook of unknown Documenso document {}", documensoDocumentId);
      return;
    }

    archiveSignedDocument(
        document.get(), parseDocumensoInstant(payload.getPayload().getCompletedAt()));
  }

  public static Instant parseDocumensoInstant(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return Instant.parse(raw);
    } catch (DateTimeParseException e) {
      log.warn("Unreadable Documenso date '{}'", raw);
      return null;
    }
  }

  @Transactional
  public void archiveSignedDocument(DocumensoDocument document, Instant signedAt) {
    try {
      var archivedAt = Instant.now();
      downloadAndSaveSignedDocument(document, document.getDocumensoDocumentId(), archivedAt);
      markDocumentCompleted(document, signedAt, archivedAt);
    } catch (RestClientException e) {
      throw new ApiException(ExceptionType.SERVER_EXCEPTION, e);
    }
  }

  private void downloadAndSaveSignedDocument(
      DocumensoDocument document, Long documensoDocumentId, Instant archivedAt) {
    var signedFile = documensoClient.downloadSignedDocument(documensoDocumentId);
    try {
      var bucketKey =
          String.format(
              "%s/%s/%d.pdf", BUCKET_FOLDER, MONTH_FOLDER.format(archivedAt), documensoDocumentId);
      bucketComponent.upload(signedFile, bucketKey);

      var fileInfo =
          fileInfoRepository.save(
              FileInfo.builder()
                  .name(bucketKey)
                  .fileType(FileType.OTHER)
                  .filePath(bucketKey)
                  .build());

      document.setFileInfo(fileInfo);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void markDocumentCompleted(
      DocumensoDocument document, Instant signedAt, Instant archivedAt) {
    if (signedAt == null) {
      log.warn(
          "Documenso gave no completion date for document {}, falling back to the archiving date",
          document.getDocumensoDocumentId());
    }
    document.setStatus(DocumensoDocumentStatus.COMPLETED);
    document.setCompletedDatetime(signedAt == null ? archivedAt : signedAt);
    document.setArchivedDatetime(archivedAt);
    documensoDocumentRepository.save(document);
  }
}
