package school.hei.haapi.service.documenso;

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
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200ResponseFieldsInner;

class PrefillFieldsFactoryTest {
  private final PrefillFieldsFactory subject = new PrefillFieldsFactory();

  private static final PersonSnapshot STUDENT =
      new PersonSnapshot("Axel HEI", "101234567890", "Lot II A Antananarivo", "0341234567");

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
      List<TemplateGetTemplateById200ResponseFieldsInner> fields, StudentLevel level) {
    return subject.buildPrefillFields(fields, STUDENT, level).stream()
        .collect(Collectors.toMap(f -> f.getId().longValue(), f -> f.getValue()));
  }

  @Test
  void no_field_yields_no_prefill() {
    assertTrue(subject.buildPrefillFields(null, STUDENT, L1).isEmpty());
    assertTrue(subject.buildPrefillFields(List.of(), STUDENT, L1).isEmpty());
  }

  @Test
  void non_text_fields_are_ignored() {
    var signature =
        new TemplateGetTemplateById200ResponseFieldsInner()
            .id(BigDecimal.valueOf(1))
            .type("SIGNATURE")
            .label("Nom et prénoms");

    assertTrue(subject.buildPrefillFields(List.of(signature), STUDENT, L1).isEmpty());
  }

  @Test
  void every_student_field_is_filled() {
    var fields =
        List.of(
            field(10, "Nom et prénoms", 100),
            field(11, "Niveau", 110),
            field(12, "Titulaire de la CIN", 120),
            field(13, "Adresse personnelle", 130),
            field(14, "Téléphones", 140));

    var byId = prefill(fields, L1);

    assertEquals(STUDENT.fullName(), byId.get(10L));
    assertEquals("Première année de Licence", byId.get(11L));
    assertEquals(STUDENT.nic(), byId.get(12L));
    assertEquals(STUDENT.address(), byId.get(13L));
    assertEquals(STUDENT.phone(), byId.get(14L));
  }

  @Test
  void a_null_level_leaves_the_level_field_empty() {
    var fields = List.of(field(10, "Nom et prénoms", 100), field(11, "Niveau", 110));

    var byId = prefill(fields, null);

    assertEquals(STUDENT.fullName(), byId.get(10L));
    assertFalse(byId.containsKey(11L), "the level field must stay untouched");
  }

  @Test
  void the_guardian_block_above_is_left_to_the_monitor() {
    var fields =
        List.of(
            field(20, "Adresse personnelle", 110),
            field(21, "Téléphones", 120),
            field(30, "Adresse personnelle", 410),
            field(31, "Téléphones", 420));

    var byId = prefill(fields, L1);

    assertFalse(byId.containsKey(20L), "the guardian's address belongs to the monitor to fill");
    assertFalse(byId.containsKey(21L), "the guardian's phone belongs to the monitor to fill");
    assertEquals(STUDENT.address(), byId.get(30L));
    assertEquals(STUDENT.phone(), byId.get(31L));
  }

  @Test
  void a_lone_field_describes_the_student() {
    var fields = List.of(field(21, "Adresse personnelle", 110));

    var byId = prefill(fields, L1);

    assertEquals(STUDENT.address(), byId.get(21L));
  }

  @Test
  void a_blank_value_is_never_sent() {
    var studentWithoutNic = new PersonSnapshot("Axel HEI", "  ", null, "0341234567");
    var fields =
        List.of(field(12, "Titulaire de la CIN", 120), field(13, "Adresse personnelle", 130));

    var prefillFields = subject.buildPrefillFields(fields, studentWithoutNic, L1);

    assertTrue(prefillFields.isEmpty(), "blank and null values must be skipped");
  }

  @Test
  void the_lowest_occurrence_wins_across_pages() {
    var onPageOne = field(40, "Nom et prénoms", 900);
    var onPageTwo =
        new TemplateGetTemplateById200ResponseFieldsInner()
            .id(BigDecimal.valueOf(41))
            .type("TEXT")
            .label("Nom et prénoms")
            .page(BigDecimal.TWO)
            .positionY(BigDecimal.valueOf(10));

    var byId = prefill(List.of(onPageOne, onPageTwo), L1);

    assertEquals(STUDENT.fullName(), byId.get(41L), "page ranks before the vertical position");
    assertFalse(byId.containsKey(40L));
  }
}
