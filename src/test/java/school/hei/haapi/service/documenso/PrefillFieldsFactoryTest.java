package school.hei.haapi.service.documenso;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.PersonSnapshot;
import school.hei.haapi.model.TemplateDocumenso;
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200ResponseFieldsInner;

class PrefillFieldsFactoryTest {
  private final PrefillFieldsFactory subject = new PrefillFieldsFactory();

  private static final PersonSnapshot STUDENT =
      new PersonSnapshot("Axel HEI", "101234567890", "Lot II A Antananarivo", "0341234567");
  private static final PersonSnapshot MONITOR =
      new PersonSnapshot("Mialy Monitor", "201234567890", "Lot III B Antsirabe", "0349876543");

  private static TemplateDocumenso template(String title) {
    return TemplateDocumenso.builder().id(randomUUID().toString()).title(title).build();
  }

  private static TemplateGetTemplateById200ResponseFieldsInner field(
      long id, String label, double positionY) {
    return new TemplateGetTemplateById200ResponseFieldsInner()
        .id(BigDecimal.valueOf(id))
        .type("TEXT")
        .label(label)
        .page(BigDecimal.ONE)
        .positionY(BigDecimal.valueOf(positionY));
  }

  private Map<Long, String> prefill(
      TemplateDocumenso template,
      List<TemplateGetTemplateById200ResponseFieldsInner> fields,
      StudentLevel level) {
    return subject.buildPrefillFields(template, fields, STUDENT, MONITOR, level).stream()
        .collect(Collectors.toMap(f -> f.getId().longValue(), f -> f.getValue()));
  }

  @Test
  void no_field_yields_no_prefill() {
    assertTrue(
        subject.buildPrefillFields(template("Attestation"), null, STUDENT, MONITOR, L1).isEmpty());
    assertTrue(
        subject
            .buildPrefillFields(template("Attestation"), List.of(), STUDENT, MONITOR, L1)
            .isEmpty());
  }

  @Test
  void non_text_fields_are_ignored() {
    var signature =
        new TemplateGetTemplateById200ResponseFieldsInner()
            .id(BigDecimal.valueOf(1))
            .type("SIGNATURE")
            .label("Nom et prénoms");

    assertTrue(
        subject
            .buildPrefillFields(template("Attestation"), List.of(signature), STUDENT, MONITOR, L1)
            .isEmpty());
  }

  @Test
  void default_template_fills_the_student_only() {
    var fields =
        List.of(
            field(10, "Nom et prénoms", 100),
            field(11, "Niveau", 110),
            field(12, "Titulaire de la CIN", 120),
            field(13, "Adresse personnelle", 130),
            field(14, "Téléphones", 140));

    var byId = prefill(template("Attestation de scolarité"), fields, L1);

    assertEquals(STUDENT.fullName(), byId.get(10L));
    assertEquals("Première année de Licence", byId.get(11L));
    assertEquals(STUDENT.nic(), byId.get(12L));
    assertEquals(STUDENT.address(), byId.get(13L));
    assertEquals(STUDENT.phone(), byId.get(14L));
  }

  @Test
  void a_null_level_leaves_the_level_field_empty() {
    var fields = List.of(field(10, "Nom et prénoms", 100), field(11, "Niveau", 110));

    var byId = prefill(template("Attestation de scolarité"), fields, null);

    assertEquals(STUDENT.fullName(), byId.get(10L));
    assertFalse(byId.containsKey(11L), "the level field must stay untouched");
  }

  @Test
  void engagement_template_gives_the_topmost_block_to_the_monitor() {
    var fields =
        List.of(
            field(20, "PERE/ MERE/ TUTEUR", 100),
            field(21, "Adresse personnelle", 110),
            field(22, "Téléphones", 120),
            field(30, "Nom et prénoms", 400),
            field(31, "Adresse personnelle", 410),
            field(32, "Téléphones", 420));

    var byId = prefill(template("Fiche d'engagement L1"), fields, L1);

    assertEquals(MONITOR.fullName(), byId.get(20L));
    assertEquals(MONITOR.address(), byId.get(21L));
    assertEquals(MONITOR.phone(), byId.get(22L));
    assertEquals(STUDENT.fullName(), byId.get(30L));
    assertEquals(STUDENT.address(), byId.get(31L));
    assertEquals(STUDENT.phone(), byId.get(32L));
  }

  @Test
  void a_lone_paired_field_goes_to_the_student_not_the_monitor() {
    var fields =
        List.of(field(20, "PERE/ MERE/ TUTEUR", 100), field(21, "Adresse personnelle", 110));

    var byId = prefill(template("Fiche d'engagement"), fields, L1);

    assertEquals(MONITOR.fullName(), byId.get(20L));
    assertEquals(STUDENT.address(), byId.get(21L), "a single address field describes the student");
  }

  @Test
  void a_blank_value_is_never_sent() {
    var studentWithoutNic = new PersonSnapshot("Axel HEI", "  ", null, "0341234567");
    var fields =
        List.of(field(12, "Titulaire de la CIN", 120), field(13, "Adresse personnelle", 130));

    var prefillFields =
        subject.buildPrefillFields(template("Attestation"), fields, studentWithoutNic, MONITOR, L1);

    assertTrue(prefillFields.isEmpty(), "blank and null values must be skipped");
  }

  @Test
  void a_missing_parent_block_is_skipped() {
    var fields = List.of(field(30, "Nom et prénoms", 400));

    var byId = prefill(template("Fiche d'engagement"), fields, L1);

    assertEquals(STUDENT.fullName(), byId.get(30L));
    assertEquals(1, byId.size());
  }
}
