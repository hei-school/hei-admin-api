package school.hei.haapi.service;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.TemplateDocumensoRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.documenso.DocumensoClient;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequest;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequestRecipientsInner;
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200ResponseFieldsInner;

@Service
@AllArgsConstructor
public class DocumensoDocumentService {
  private static final Map<String, java.util.function.Function<StudentSnapshot, String>>
      FIELD_LABEL_MATCHERS =
          Map.of(
              "nom et prenom", s -> s.fullName,
              "inscrit", s -> s.levelLabel,
              "cin", s -> s.nic,
              "adresse personnelle", s -> s.address,
              "telephone", s -> s.phone);

  private final DocumensoClient documensoClient;
  private final DocumensoDocumentRepository documensoDocumentRepository;
  private final DocumensoDocumentRecipientRepository documensoDocumentRecipientRepository;
  private final TemplateDocumensoRepository templateDocumensoRepository;
  private final UserRepository userRepository;
  private final MonitoringStudentRepository monitoringStudentRepository;
  private final FeeRepository feeRepository;
  private final FileInfoRepository fileInfoRepository;
  private final BucketComponent bucketComponent;

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
    var template = resolveTemplateByName(templateName, level);
    var documensoTemplateId = template.getDocumensoTemplateId();

    try {
      var remoteTemplate = documensoClient.getTemplate(documensoTemplateId);
      var placeholders = remoteTemplate.getRecipients();
      if (placeholders == null || placeholders.isEmpty()) {
        throw new ApiException(
            SERVER_EXCEPTION,
            "Documenso template " + documensoTemplateId + " must define a recipient placeholder");
      }

      var request = new TemplateCreateDocumentFromTemplateRequest();
      request.setTemplateId(BigDecimal.valueOf(documensoTemplateId));
      request.setRecipients(List.of(toRecipient(placeholders.get(0).getId(), monitor)));
      request.setPrefillFields(
          buildPrefillFields(remoteTemplate.getFields(), new StudentSnapshot(student, level)));

      var response = documensoClient.useTemplate(request);

      var document =
          documensoDocumentRepository.save(
              DocumensoDocument.builder()
                  .documensoDocumentId(response.getId().longValue())
                  .template(template)
                  .student(student)
                  .level(level)
                  .status(DocumensoDocument.Status.PENDING)
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
    } catch (school.hei.haapi.service.documenso.gen.invoker.ApiException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  private school.hei.haapi.model.TemplateDocumenso resolveTemplateByName(
      String templateName, StudentLevel level) {
    var candidates = templateDocumensoRepository.findAllByTitleContainingIgnoreCase(templateName);
    if (candidates.isEmpty()) {
      throw new NotFoundException("No synced Documenso template matching: " + templateName);
    }
    if (candidates.size() == 1) {
      return candidates.get(0);
    }
    if (level != null) {
      var matchingLevel =
          candidates.stream()
              .filter(
                  candidate -> normalize(candidate.getTitle()).contains(normalize(level.name())))
              .toList();
      if (matchingLevel.size() == 1) {
        return matchingLevel.get(0);
      }
    }
    throw new ApiException(
        SERVER_EXCEPTION,
        "Several Documenso templates match \""
            + templateName
            + "\" and the student's level doesn't disambiguate them: "
            + candidates.stream().map(school.hei.haapi.model.TemplateDocumenso::getTitle).toList());
  }

  private TemplateCreateDocumentFromTemplateRequestRecipientsInner toRecipient(
      BigDecimal placeholderId, User user) {
    var recipient = new TemplateCreateDocumentFromTemplateRequestRecipientsInner();
    recipient.setId(placeholderId);
    recipient.setEmail(user.getEmail());
    recipient.setName(user.getFirstName() + " " + user.getLastName());
    return recipient;
  }

  private List<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner> buildPrefillFields(
      List<TemplateGetTemplateById200ResponseFieldsInner> fields, StudentSnapshot student) {
    var prefillFields =
        new ArrayList<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner>();
    if (fields == null) {
      return prefillFields;
    }
    for (var field : fields) {
      if (!"TEXT".equalsIgnoreCase(field.getType())) {
        continue;
      }
      var label = field.getLabel() != null ? field.getLabel() : field.getPlaceholder();
      if (label == null) {
        continue;
      }
      var normalizedLabel = normalize(label);
      FIELD_LABEL_MATCHERS.entrySet().stream()
          .filter(matcher -> normalizedLabel.contains(matcher.getKey()))
          .findFirst()
          .map(matcher -> matcher.getValue().apply(student))
          .filter(value -> value != null && !value.isBlank())
          .ifPresent(value -> prefillFields.add(toPrefillField(field.getId(), value)));
    }
    return prefillFields;
  }

  private static String normalize(String value) {
    var withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    return withoutAccents.toLowerCase(Locale.FRENCH);
  }

  private TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner toPrefillField(
      BigDecimal fieldId, String value) {
    var field = new TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner();
    field.setId(fieldId);
    field.setType(TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner.TypeEnum.TEXT);
    field.setValue(value);
    return field;
  }

  private record StudentSnapshot(
      String fullName, String nic, String address, String phone, String levelLabel) {
    StudentSnapshot(User student, StudentLevel level) {
      this(
          student.getFirstName() + " " + student.getLastName(),
          student.getNic(),
          student.getAddress(),
          student.getPhone(),
          level == null ? null : Promotion.getLevelString(level));
    }
  }

  private StudentLevel safeLevelAt(User student) {
    return student
        .findCurrentGroup()
        .flatMap(
            group -> {
              try {
                return java.util.Optional.of(group.getPromotion().getLevelAt(Instant.now()));
              } catch (PromotionLevelOutOfRangeException e) {
                return java.util.Optional.empty();
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
            .orElseThrow(() -> new NotFoundException("Documenso document " + documensoDocumentId));

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
