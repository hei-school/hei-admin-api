package school.hei.haapi.service.documenso;

import static school.hei.haapi.model.Promotion.getLevelString;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.DocumensoTemplateFieldLabels;
import school.hei.haapi.model.PersonSnapshot;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner;
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200ResponseFieldsInner;

@Component
public class PrefillFieldsFactory {

  public List<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner> buildPrefillFields(
      List<TemplateGetTemplateById200ResponseFieldsInner> fields,
      PersonSnapshot student,
      StudentLevel level) {
    if (fields == null || fields.isEmpty()) {
      return List.of();
    }
    var textFields = fields.stream().filter(field -> isText(field)).toList();
    var prefillFields =
        new ArrayList<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner>();

    addStudentField(
        textFields, prefillFields, DocumensoTemplateFieldLabels.FULL_NAME, student.fullName());
    addStudentField(
        textFields,
        prefillFields,
        DocumensoTemplateFieldLabels.LEVEL,
        level == null ? null : getLevelString(level));
    addStudentField(
        textFields, prefillFields, DocumensoTemplateFieldLabels.ADDRESS, student.address());
    addStudentField(textFields, prefillFields, DocumensoTemplateFieldLabels.PHONE, student.phone());
    addStudentField(textFields, prefillFields, DocumensoTemplateFieldLabels.NIC, student.nic());

    return prefillFields;
  }

  private void addStudentField(
      List<TemplateGetTemplateById200ResponseFieldsInner> textFields,
      List<TemplateCreateDocumentFromTemplateRequestPrefillFieldsInner> prefillFields,
      String label,
      String value) {
    if (value == null || value.isBlank()) {
      return;
    }
    var candidates = fieldsMatching(textFields, label);
    if (candidates.isEmpty()) {
      return;
    }
    prefillFields.add(toPrefillField(candidates.getLast().getId(), value));
  }

  private List<TemplateGetTemplateById200ResponseFieldsInner> fieldsMatching(
      List<TemplateGetTemplateById200ResponseFieldsInner> fields, String labelKeyword) {
    return fields.stream()
        .filter(field -> labelContains(field, labelKeyword))
        .sorted(
            Comparator.comparing(
                    (TemplateGetTemplateById200ResponseFieldsInner field) ->
                        orZero(field.getPage()))
                .thenComparing(field -> orZero(field.getPositionY())))
        .toList();
  }

  private static boolean isText(TemplateGetTemplateById200ResponseFieldsInner field) {
    return "TEXT".equalsIgnoreCase(field.getType());
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
}
