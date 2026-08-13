package school.hei.haapi.service.documenso;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.PersonSnapshot;
import school.hei.haapi.model.TemplateDocumenso;
import school.hei.haapi.model.DocumensoTemplateFieldLabels;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner;
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200ResponseFieldsInner;

@Component
@AllArgsConstructor
public class PrefillFieldsFactory {
  private final DocumensoTemplateResolver templateResolver;

  public List<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner> buildPrefillFields(
      TemplateDocumenso template,
      List<TemplateGetTemplateById200ResponseFieldsInner> fields,
      PersonSnapshot student,
      PersonSnapshot monitor,
      StudentLevel level) {
    if (fields == null || fields.isEmpty()) {
      return List.of();
    }
    var textFields = fields.stream().filter(f -> "TEXT".equalsIgnoreCase(f.getType())).toList();
    if (normalize(template.getTitle()).contains("engagement")) {
      return buildFicheEngagementFields(textFields, student, monitor, level);
    }
    return buildDefaultFields(textFields, student, level);
  }

  private List<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner>
      buildFicheEngagementFields(
          List<TemplateGetTemplateById200ResponseFieldsInner> textFields,
          PersonSnapshot student,
          PersonSnapshot monitor,
          StudentLevel level) {
    var prefillFields =
        new ArrayList<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner>();

    matchOnly(textFields, DocumensoTemplateFieldLabels.FULL_NAME, student.fullName())
        .ifPresent(prefillFields::add);
    matchOnly(textFields, DocumensoTemplateFieldLabels.LEVEL, level == null ? null : getLevelString(level))
        .ifPresent(prefillFields::add);
    matchByPosition(textFields, DocumensoTemplateFieldLabels.PARENT_INDICATOR, monitor.fullName(), true)
        .ifPresent(prefillFields::add);

    for (var label :
        List.of(DocumensoTemplateFieldLabels.ADDRESS, DocumensoTemplateFieldLabels.PHONE, DocumensoTemplateFieldLabels.NIC)) {
      addFieldPairIfFound(textFields, prefillFields, label, monitor, student);
    }
    return prefillFields;
  }

  private List<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner> buildDefaultFields(
      List<TemplateGetTemplateById200ResponseFieldsInner> textFields,
      PersonSnapshot student,
      StudentLevel level) {
    var prefillFields =
        new ArrayList<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner>();
    matchOnly(textFields, DocumensoTemplateFieldLabels.FULL_NAME, student.fullName())
        .ifPresent(prefillFields::add);
    matchOnly(textFields, DocumensoTemplateFieldLabels.LEVEL, level == null ? null : getLevelString(level))
        .ifPresent(prefillFields::add);
    matchOnly(textFields, DocumensoTemplateFieldLabels.NIC, student.nic()).ifPresent(prefillFields::add);
    matchOnly(textFields, DocumensoTemplateFieldLabels.ADDRESS, student.address())
        .ifPresent(prefillFields::add);
    matchOnly(textFields, DocumensoTemplateFieldLabels.PHONE, student.phone()).ifPresent(prefillFields::add);
    return prefillFields;
  }

  private void addFieldPairIfFound(
      List<TemplateGetTemplateById200ResponseFieldsInner> textFields,
      ArrayList<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner> prefillFields,
      String label,
      PersonSnapshot monitor,
      PersonSnapshot student) {
    var candidates = fieldsMatching(textFields, label);
    if (candidates.size() >= 2) {
      matchAt(candidates.getFirst(), monitor.field(label)).ifPresent(prefillFields::add);
      matchAt(candidates.get(candidates.size() - 1), student.field(label))
          .ifPresent(prefillFields::add);
    } else if (candidates.size() == 1) {
      matchAt(candidates.getFirst(), student.field(label)).ifPresent(prefillFields::add);
    }
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
    var chosen = topmost ? candidates.getFirst() : candidates.get(candidates.size() - 1);
    return matchAt(chosen, value);
  }

  private List<TemplateGetTemplateById200ResponseFieldsInner> fieldsMatching(
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

  private String getLevelString(StudentLevel level) {
    return school.hei.haapi.model.Promotion.getLevelString(level);
  }
}
