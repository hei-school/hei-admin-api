package school.hei.haapi.service;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.DocumensoDocument;
import school.hei.haapi.model.DocumensoDocumentRecipient;
import school.hei.haapi.model.PersonSnapshot;
import school.hei.haapi.model.TemplateDocumenso;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.promotion.PromotionLevelOutOfRangeException;
import school.hei.haapi.repository.DocumensoDocumentRecipientRepository;
import school.hei.haapi.repository.DocumensoDocumentRepository;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.documenso.DocumensoClient;
import school.hei.haapi.service.documenso.DocumensoTemplateResolver;
import school.hei.haapi.service.documenso.DocumensoWebhookHandler;
import school.hei.haapi.service.documenso.DocumensoWebhookPayload;
import school.hei.haapi.service.documenso.PrefillFieldsFactory;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplate200Response;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequest;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequestRecipientsInner;
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200Response;

@Service
@AllArgsConstructor
public class DocumensoDocumentService {
  private final DocumensoClient documensoClient;
  private final DocumensoDocumentRepository documensoDocumentRepository;
  private final DocumensoDocumentRecipientRepository documensoDocumentRecipientRepository;
  private final UserRepository userRepository;
  private final MonitoringStudentRepository monitoringStudentRepository;
  private final DocumensoTemplateResolver templateResolver;
  private final PrefillFieldsFactory prefillFieldsFactory;
  private final DocumensoWebhookHandler webhookHandler;

  @Transactional
  public DocumensoDocument generateDocument(String studentId, String templateName) {
    var student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new NotFoundException("User with id: " + studentId));
    var monitor =
        monitoringStudentRepository.findAllMonitorsByStudentId(studentId).stream()
            .findFirst()
            .orElseThrow(() -> new NotFoundException("No monitor linked to student " + studentId));
    var level = safeLevelAt(student);
    var template = templateResolver.resolveByName(templateName, level);

    try {
      var remoteTemplate = documensoClient.getTemplate(template.getDocumensoTemplateId());
      validateRemoteTemplate(remoteTemplate, template.getDocumensoTemplateId());

      var request = buildDocumentRequest(remoteTemplate, template, student, monitor, level);
      var response = documensoClient.useTemplate(request);

      return persistDocument(template, student, level, monitor, response);
    } catch (RestClientException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  private TemplateCreateDocumentFromTemplateRequest buildDocumentRequest(
      TemplateGetTemplateById200Response remoteTemplate,
      TemplateDocumenso template,
      User student,
      User monitor,
      StudentLevel level) {
    var request = new TemplateCreateDocumentFromTemplateRequest();
    request.setTemplateId(BigDecimal.valueOf(remoteTemplate.getId().longValue()));
    request.setRecipients(
        List.of(toRecipient(remoteTemplate.getRecipients().getFirst().getId(), monitor)));
    request.setPrefillFields(
        prefillFieldsFactory.buildPrefillFields(
            template,
            remoteTemplate.getFields(),
            new PersonSnapshot(student),
            new PersonSnapshot(monitor),
            level));
    return request;
  }

  private void validateRemoteTemplate(
      TemplateGetTemplateById200Response remoteTemplate, Long documensoTemplateId) {
    var placeholders = remoteTemplate.getRecipients();
    if (placeholders == null || placeholders.isEmpty()) {
      throw new ApiException(
          SERVER_EXCEPTION,
          "Documenso template " + documensoTemplateId + " must define a recipient placeholder");
    }
  }

  private DocumensoDocument persistDocument(
      TemplateDocumenso template,
      User student,
      StudentLevel level,
      User monitor,
      TemplateCreateDocumentFromTemplate200Response response) {
    var document =
        documensoDocumentRepository.save(
            DocumensoDocument.builder()
                .documensoDocumentId(response.getId().longValue())
                .template(template)
                .student(student)
                .level(level)
                .status(school.hei.haapi.model.DocumensoDocumentStatus.PENDING)
                .build());

    for (var recipient : response.getRecipients()) {
      documensoDocumentRecipientRepository.save(
          DocumensoDocumentRecipient.builder()
              .document(document)
              .user(monitor)
              .documensoRecipientId(recipient.getId().longValue())
              .signingToken(recipient.getToken())
              .build());
    }
    return document;
  }

  private TemplateCreateDocumentFromTemplateRequestRecipientsInner toRecipient(
      BigDecimal placeholderId, User user) {
    var recipient = new TemplateCreateDocumentFromTemplateRequestRecipientsInner();
    recipient.setId(placeholderId);
    recipient.setEmail(user.getEmail());
    recipient.setName(user.getFirstName() + " " + user.getLastName());
    return recipient;
  }

  private StudentLevel safeLevelAt(User student) {
    return student
        .findCurrentGroup()
        .flatMap(
            group -> {
              try {
                return Optional.of(group.getPromotion().getLevelAt(Instant.now()));
              } catch (PromotionLevelOutOfRangeException e) {
                return Optional.empty();
              }
            })
        .orElse(null);
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
  public void handleWebhook(DocumensoWebhookPayload payload) {
    webhookHandler.handle(payload);
  }
}
