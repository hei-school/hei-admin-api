package school.hei.haapi.service;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.DocumensoDocument;
import school.hei.haapi.model.DocumensoDocumentRecipient;
import school.hei.haapi.model.DocumensoDocumentStatus;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.PersonSnapshot;
import school.hei.haapi.model.TemplateDocumenso;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.ForbiddenException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.promotion.PromotionLevelOutOfRangeException;
import school.hei.haapi.repository.DocumensoDocumentRecipientRepository;
import school.hei.haapi.repository.DocumensoDocumentRepository;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.DocumensoDocumentDao;
import school.hei.haapi.service.aws.FileService;
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
  private final DocumensoDocumentDao documensoDocumentDao;
  private final DocumensoDocumentRecipientRepository documensoDocumentRecipientRepository;
  private final UserRepository userRepository;
  private final MonitoringStudentRepository monitoringStudentRepository;
  private final DocumensoTemplateResolver templateResolver;
  private final PrefillFieldsFactory prefillFieldsFactory;
  private final DocumensoWebhookHandler webhookHandler;
  private final FileService fileService;
  private static final Set<User.Role> STAFF_ROLES = Set.of(User.Role.ADMIN, User.Role.MANAGER);
  private static final long SIGNED_FILE_LINK_LIFETIME_SECONDS = 300L;

  private static final List<DocumensoDocumentStatus> STILL_STANDING =
      List.of(DocumensoDocumentStatus.PENDING, DocumensoDocumentStatus.COMPLETED);

  @Transactional
  public DocumensoDocument generateDocument(
      String studentId, String templateName, String generatedById) {
    var student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new NotFoundException("User with id: " + studentId));
    var generatedBy =
        userRepository
            .findById(generatedById)
            .orElseThrow(() -> new NotFoundException("User with id: " + generatedById));
    var monitor =
        monitoringStudentRepository.findAllMonitorsByStudentId(studentId).stream()
            .findFirst()
            .orElseThrow(() -> new NotFoundException("No monitor linked to student " + studentId));
    var level = safeLevelAt(student);
    var template = templateResolver.resolveByName(templateName, level);

    var stillStanding =
        documensoDocumentRepository.findFirstBySubject_IdAndTemplate_IdAndStatusIn(
            student.getId(), template.getId(), STILL_STANDING);
    if (stillStanding.isPresent()) {
      return stillStanding.get();
    }

    try {
      var remoteTemplate = documensoClient.getTemplate(template.getDocumensoTemplateId());
      validateRemoteTemplate(remoteTemplate, template);

      var request = buildDocumentRequest(remoteTemplate, template, student, monitor, level);
      var response = documensoClient.useTemplate(request);

      return persistDocument(template, student, level, monitor, generatedBy, response);
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
    request.setDistributeDocument(true);
    request.setRecipients(
        List.of(toRecipient(remoteTemplate.getRecipients().getFirst().getId(), monitor)));
    request.setPrefillFields(
        prefillFieldsFactory.buildPrefillFields(
            remoteTemplate.getFields(), new PersonSnapshot(student), level));
    return request;
  }

  private void validateRemoteTemplate(
      TemplateGetTemplateById200Response remoteTemplate, TemplateDocumenso template) {
    var placeholders = remoteTemplate.getRecipients();
    if (placeholders == null || placeholders.size() != 1) {
      throw new ApiException(
          SERVER_EXCEPTION,
          "Documenso template "
              + template.getDocumensoTemplateId()
              + " must define exactly one signer, the monitor, the admin's signature belonging to"
              + " the template's PDF: found "
              + (placeholders == null ? 0 : placeholders.size()));
    }
  }

  private DocumensoDocument persistDocument(
      TemplateDocumenso template,
      User student,
      StudentLevel level,
      User monitor,
      User generatedBy,
      TemplateCreateDocumentFromTemplate200Response response) {
    var document =
        documensoDocumentRepository.save(
            DocumensoDocument.builder()
                .documensoDocumentId(response.getId().longValue())
                .template(template)
                .subject(student)
                .level(level)
                .status(DocumensoDocumentStatus.PENDING)
                .generatedBy(generatedBy)
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

  public List<DocumensoDocument> getByMonitorId(
      String monitorId, PageFromOne page, BoundedPageSize pageSize) {
    var pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());
    return documensoDocumentRepository.findAllByMonitorId(monitorId, pageable);
  }

  public List<DocumensoDocument> getByPromotionId(
      String promotionId,
      StudentLevel level,
      DocumensoDocumentStatus status,
      PageFromOne page,
      BoundedPageSize pageSize) {
    var studentIds =
        userRepository.findAllStudentsByPromotionId(promotionId).stream().map(User::getId).toList();
    var pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());
    return documensoDocumentDao.filterByCriteria(studentIds, level, status, pageable);
  }

  public String getSignedFileUrl(String documentId, String requestingUserId) {
    var document =
        documensoDocumentRepository
            .findById(documentId)
            .orElseThrow(() -> new NotFoundException("Documenso document " + documentId));
    assertMayOpen(document, requestingUserId);

    var fileInfo = document.getFileInfo();
    if (fileInfo == null) {
      throw new NotFoundException(
          "Documenso document "
              + documentId
              + " holds no signed file yet: it is "
              + document.getStatus());
    }
    return fileService.getPresignedUrl(fileInfo.getFilePath(), SIGNED_FILE_LINK_LIFETIME_SECONDS);
  }

  private void assertMayOpen(DocumensoDocument document, String requestingUserId) {
    var requester =
        userRepository
            .findById(requestingUserId)
            .orElseThrow(() -> new NotFoundException("User with id: " + requestingUserId));
    if (STAFF_ROLES.contains(requester.getRole())) {
      return;
    }
    var followsSubject =
        monitoringStudentRepository
            .findAllMonitorsByStudentId(document.getSubject().getId())
            .stream()
            .anyMatch(monitor -> monitor.getId().equals(requestingUserId));
    if (followsSubject) {
      return;
    }
    throw new ForbiddenException(
        "User "
            + requestingUserId
            + " does not follow the subject of document "
            + document.getId());
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
