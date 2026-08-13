package school.hei.haapi.service.documenso;

import java.time.Instant;
import lombok.AllArgsConstructor;
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
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.DocumensoDocumentRepository;
import school.hei.haapi.repository.FileInfoRepository;

@Component
@AllArgsConstructor
public class DocumensoWebhookHandler {
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
    var document =
        documensoDocumentRepository
            .findByDocumensoDocumentId(documensoDocumentId)
            .orElseThrow(() -> new NotFoundException("Documenso document " + documensoDocumentId));

    try {
      downloadAndSaveSignedDocument(document, documensoDocumentId);
      markDocumentCompleted(document);
    } catch (RestClientException e) {
      throw new ApiException(ExceptionType.SERVER_EXCEPTION, e);
    }
  }

  private void downloadAndSaveSignedDocument(DocumensoDocument document, Long documensoDocumentId) {
    var signedFile = documensoClient.downloadSignedDocument(documensoDocumentId);
    var bucketKey = "documenso-documents/" + documensoDocumentId + ".pdf";
    bucketComponent.upload(signedFile, bucketKey);

    var fileInfo =
        fileInfoRepository.save(
            FileInfo.builder()
                .name(bucketKey)
                .fileType(FileType.OTHER)
                .filePath(bucketKey)
                .build());

    document.setFileInfo(fileInfo);
  }

  private void markDocumentCompleted(DocumensoDocument document) {
    document.setStatus(DocumensoDocumentStatus.COMPLETED);
    document.setCompletedDatetime(Instant.now());
    documensoDocumentRepository.save(document);
  }
}
