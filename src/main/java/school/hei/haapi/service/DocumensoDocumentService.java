package school.hei.haapi.service;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
          buildPrefillFields(template, remoteTemplate.getFields(), student, monitor, level));

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
    } catch (org.springframework.web.client.RestClientException e) {
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
      school.hei.haapi.model.TemplateDocumenso template,
      List<TemplateGetTemplateById200ResponseFieldsInner> fields,
      User student,
      User monitor,
      StudentLevel level) {
    if (fields == null || fields.isEmpty()) {
      return List.of();
    }
    var textFields = fields.stream().filter(f -> "TEXT".equalsIgnoreCase(f.getType())).toList();
    if (normalize(template.getTitle()).contains("engagement")) {
      return buildFicheEngagementPrefillFields(
          textFields, new PersonSnapshot(student), new PersonSnapshot(monitor), level);
    }
    return buildDefaultPrefillFields(textFields, new PersonSnapshot(student), level);
  }

  private List<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner>
      buildFicheEngagementPrefillFields(
          List<TemplateGetTemplateById200ResponseFieldsInner> textFields,
          PersonSnapshot student,
          PersonSnapshot monitor,
          StudentLevel level) {
    var prefillFields =
        new ArrayList<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner>();

    matchOnly(textFields, "nom et prenom", student.fullName()).ifPresent(prefillFields::add);
    matchOnly(textFields, "inscrit", level == null ? null : Promotion.getLevelString(level))
        .ifPresent(prefillFields::add);
    matchByPosition(textFields, "pere/", monitor.fullName(), true).ifPresent(prefillFields::add);
    for (var keyword : List.of("adresse personnelle", "telephone", "titulaire de la cin")) {
      var candidates = fieldsMatching(textFields, keyword);
      if (candidates.size() >= 2) {
        matchAt(candidates.get(0), monitor.field(keyword)).ifPresent(prefillFields::add);
        matchAt(candidates.get(candidates.size() - 1), student.field(keyword))
            .ifPresent(prefillFields::add);
      } else if (candidates.size() == 1) {
        matchAt(candidates.get(0), student.field(keyword)).ifPresent(prefillFields::add);
      }
    }
    return prefillFields;
  }

  private List<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner>
      buildDefaultPrefillFields(
          List<TemplateGetTemplateById200ResponseFieldsInner> textFields,
          PersonSnapshot student,
          StudentLevel level) {
    var prefillFields =
        new ArrayList<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner>();
    matchOnly(textFields, "nom et prenom", student.fullName()).ifPresent(prefillFields::add);
    matchOnly(textFields, "inscrit", level == null ? null : Promotion.getLevelString(level))
        .ifPresent(prefillFields::add);
    matchOnly(textFields, "titulaire de la cin", student.nic()).ifPresent(prefillFields::add);
    matchOnly(textFields, "adresse personnelle", student.address()).ifPresent(prefillFields::add);
    matchOnly(textFields, "telephone", student.phone()).ifPresent(prefillFields::add);
    return prefillFields;
  }

  private Optional<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner> matchOnly(
      List<TemplateGetTemplateById200ResponseFieldsInner> fields,
      String labelKeyword,
      String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    return fields.stream()
        .filter(field -> labelContains(field, labelKeyword))
        .findFirst()
        .map(field -> toPrefillField(field.getId(), value));
  }

  private Optional<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner> matchByPosition(
      List<TemplateGetTemplateById200ResponseFieldsInner> fields,
      String labelKeyword,
      String value,
      boolean topmost) {
    var candidates = fieldsMatching(fields, labelKeyword);
    if (candidates.isEmpty()) {
      return Optional.empty();
    }
    var chosen = topmost ? candidates.get(0) : candidates.get(candidates.size() - 1);
    return matchAt(chosen, value);
  }

  private static List<TemplateGetTemplateById200ResponseFieldsInner> fieldsMatching(
      List<TemplateGetTemplateById200ResponseFieldsInner> fields, String labelKeyword) {
    return fields.stream()
        .filter(field -> labelContains(field, labelKeyword))
        .sorted(
            Comparator.comparing(
                    (TemplateGetTemplateById200ResponseFieldsInner f) -> orZero(f.getPage()))
                .thenComparing(f -> orZero(f.getPositionY())))
        .toList();
  }

  private Optional<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner> matchAt(
      TemplateGetTemplateById200ResponseFieldsInner field, String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    return Optional.of(toPrefillField(field.getId(), value));
  }

  private static boolean labelContains(
      TemplateGetTemplateById200ResponseFieldsInner field, String labelKeyword) {
    var label = field.getLabel() != null ? field.getLabel() : field.getPlaceholder();
    return label != null && normalize(label).contains(labelKeyword);
  }

  private static BigDecimal orZero(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
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

  private record PersonSnapshot(String fullName, String nic, String address, String phone) {
    PersonSnapshot(User user) {
      this(
          user.getFirstName() + " " + user.getLastName(),
          user.getNic(),
          user.getAddress(),
          user.getPhone());
    }

    String field(String labelKeyword) {
      return switch (labelKeyword) {
        case "adresse personnelle" -> address;
        case "telephone" -> phone;
        case "titulaire de la cin" -> nic;
        default -> null;
      };
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
    } catch (org.springframework.web.client.RestClientException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }
}
