package school.hei.haapi.service;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.DocumensoDocument;
import school.hei.haapi.model.DocumensoDocumentRecipient;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.promotion.PromotionLevelOutOfRangeException;
import school.hei.haapi.repository.DocumensoDocumentRecipientRepository;
import school.hei.haapi.repository.DocumensoDocumentRepository;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.FileInfoRepository;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.repository.TemplateDocumensoRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.documenso.DocumensoClient;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequest;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequestRecipientsInner;

@Service
@AllArgsConstructor
public class DocumensoDocumentService {
  private final DocumensoClient documensoClient;
  private final DocumensoDocumentRepository documensoDocumentRepository;
  private final DocumensoDocumentRecipientRepository documensoDocumentRecipientRepository;
  private final TemplateDocumensoRepository templateDocumensoRepository;
  private final PromotionRepository promotionRepository;
  private final UserRepository userRepository;
  private final FeeRepository feeRepository;
  private final FileInfoRepository fileInfoRepository;
  private final BucketComponent bucketComponent;

  @Transactional
  public DocumensoDocument generateForPromotionLevel(
      String promotionId,
      StudentLevel level,
      long documensoTemplateId,
      String adminId,
      String monitorId,
      Map<Long, String> prefillFieldValues) {
    var promotion =
        promotionRepository
            .findById(promotionId)
            .orElseThrow(() -> new NotFoundException("Promotion with id: " + promotionId));
    var admin =
        userRepository
            .findById(adminId)
            .orElseThrow(() -> new NotFoundException("User with id: " + adminId));
    var monitor =
        userRepository
            .findById(monitorId)
            .orElseThrow(() -> new NotFoundException("User with id: " + monitorId));
    var template =
        templateDocumensoRepository
            .findByDocumensoTemplateId(documensoTemplateId)
            .orElseThrow(
                () -> new NotFoundException("Documenso template: " + documensoTemplateId));

    try {
      var remoteTemplate = documensoClient.getTemplate(documensoTemplateId);
      var placeholders = remoteTemplate.getRecipients();
      if (placeholders == null || placeholders.size() < 2) {
        throw new ApiException(
            SERVER_EXCEPTION,
            "Documenso template "
                + documensoTemplateId
                + " must define at least 2 recipient placeholders (admin + monitor)");
      }

      var request = new TemplateCreateDocumentFromTemplateRequest();
      request.setTemplateId(BigDecimal.valueOf(documensoTemplateId));
      request.setRecipients(
          List.of(
              toRecipient(placeholders.get(0).getId(), admin),
              toRecipient(placeholders.get(1).getId(), monitor)));
      if (prefillFieldValues != null && !prefillFieldValues.isEmpty()) {
        request.setPrefillFields(
            prefillFieldValues.entrySet().stream().map(this::toPrefillField).toList());
      }

      var response = documensoClient.useTemplate(request);

      var document =
          documensoDocumentRepository.save(
              DocumensoDocument.builder()
                  .documensoDocumentId(response.getId().longValue())
                  .template(template)
                  .promotion(promotion)
                  .level(level)
                  .status(DocumensoDocument.Status.PENDING)
                  .build());

      for (var recipient : response.getRecipients()) {
        var user = recipient.getEmail().equals(admin.getEmail()) ? admin : monitor;
        documensoDocumentRecipientRepository.save(
            DocumensoDocumentRecipient.builder()
                .document(document)
                .user(user)
                .documensoRecipientId(recipient.getId().longValue())
                .signingToken(recipient.getToken())
                .build());
      }
      return document;
    } catch (school.hei.haapi.service.documenso.gen.invoker.ApiException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  private TemplateCreateDocumentFromTemplateRequestRecipientsInner toRecipient(
      BigDecimal placeholderId, User user) {
    var recipient = new TemplateCreateDocumentFromTemplateRequestRecipientsInner();
    recipient.setId(placeholderId);
    recipient.setEmail(user.getEmail());
    recipient.setName(user.getFirstName() + " " + user.getLastName());
    return recipient;
  }

  private TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner toPrefillField(
      Map.Entry<Long, String> entry) {
    var field = new TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner();
    field.setId(BigDecimal.valueOf(entry.getKey()));
    field.setType(TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner.TypeEnum.TEXT);
    field.setValue(entry.getValue());
    return field;
  }

  public List<User> findMonthlyPayingStudentsForPromotionLevel(
      String promotionId, StudentLevel level) {
    var monthlyPayers =
        feeRepository.findAllByFrequency(FeeFrequency.MONTHLY).stream()
            .map(fee -> fee.getStudent().getId())
            .collect(Collectors.toSet());
    return userRepository.findAllByRoleAndStatus(User.Role.STUDENT, User.Status.ENABLED).stream()
        .filter(student -> monthlyPayers.contains(student.getId()))
        .filter(
            student ->
                student
                    .findCurrentGroup()
                    .map(
                        group ->
                            group.getPromotion().getId().equals(promotionId)
                                && level == safeLevelAt(group.getPromotion()))
                    .orElse(false))
        .toList();
  }

  private StudentLevel safeLevelAt(Promotion promotion) {
    try {
      return promotion.getLevelAt(Instant.now());
    } catch (PromotionLevelOutOfRangeException e) {
      return null;
    }
  }

  public String getSigningToken(String documentId, String requestingUserId) {
    return documensoDocumentRecipientRepository
        .findByDocument_IdAndUser_Id(documentId, requestingUserId)
        .map(DocumensoDocumentRecipient::getSigningToken)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "No Documenso recipient for document "
                        + documentId
                        + " and user "
                        + requestingUserId));
  }

  @Transactional
  @SuppressWarnings("unchecked")
  public void handleWebhook(Map<String, Object> payload) {
    var event = String.valueOf(payload.get("event"));
    if (!event.contains("COMPLETED")) {
      return;
    }
    var data = (Map<String, Object>) payload.get("payload");
    if (data == null || data.get("id") == null) {
      return;
    }
    var documensoDocumentId = Long.parseLong(String.valueOf(data.get("id")));
    var document =
        documensoDocumentRepository
            .findByDocumensoDocumentId(documensoDocumentId)
            .orElseThrow(
                () -> new NotFoundException("Documenso document " + documensoDocumentId));

    try {
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
      document.setStatus(DocumensoDocument.Status.COMPLETED);
      document.setCompletedDatetime(Instant.now());
      documensoDocumentRepository.save(document);
    } catch (school.hei.haapi.service.documenso.gen.invoker.ApiException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }
}
