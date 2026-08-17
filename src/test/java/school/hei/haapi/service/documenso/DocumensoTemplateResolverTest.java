package school.hei.haapi.service.documenso;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.TemplateDocumenso;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.TemplateDocumensoRepository;

class DocumensoTemplateResolverTest {
  private final TemplateDocumensoRepository repository = mock(TemplateDocumensoRepository.class);
  private final DocumensoTemplateResolver subject = new DocumensoTemplateResolver(repository);

  private static TemplateDocumenso template(String title) {
    return TemplateDocumenso.builder().id(randomUUID().toString()).title(title).build();
  }

  private void givenCandidates(TemplateDocumenso... candidates) {
    when(repository.findAllByTitleContainingIgnoreCase(any())).thenReturn(List.of(candidates));
  }

  @Test
  void unknown_template_is_not_found() {
    givenCandidates();

    var thrown = assertThrows(NotFoundException.class, () -> subject.resolveByName("Fiche", null));
    assertTrue(thrown.getMessage().contains("Fiche"));
  }

  @Test
  void single_candidate_is_returned_whatever_the_level() {
    var onlyOne = template("Fiche d'engagement");
    givenCandidates(onlyOne);

    assertEquals(onlyOne, subject.resolveByName("Fiche d'engagement", null));
    assertEquals(onlyOne, subject.resolveByName("Fiche d'engagement", L1));
  }

  @Test
  void level_disambiguates_several_candidates() {
    var forL1 = template("Fiche d'engagement L1");
    var forL2 = template("Fiche d'engagement L2");
    givenCandidates(forL1, forL2);

    assertEquals(forL1, subject.resolveByName("Fiche d'engagement", L1));
    assertEquals(forL2, subject.resolveByName("Fiche d'engagement", L2));
  }

  @Test
  void level_matching_ignores_accents_and_case() {
    var forL1 = template("Fiche d'engagement é l1");
    var other = template("Fiche d'engagement M2");
    givenCandidates(forL1, other);

    assertEquals(forL1, subject.resolveByName("Fiche d'engagement", L1));
  }

  @Test
  void several_candidates_without_level_are_ambiguous() {
    givenCandidates(template("Fiche d'engagement L1"), template("Fiche d'engagement L2"));

    var thrown =
        assertThrows(ApiException.class, () -> subject.resolveByName("Fiche d'engagement", null));
    assertTrue(thrown.getMessage().contains("Fiche d'engagement"));
  }

  @Test
  void several_candidates_the_level_does_not_narrow_are_ambiguous() {
    givenCandidates(template("Fiche d'engagement L1"), template("Attestation L1"));
    assertThrows(ApiException.class, () -> subject.resolveByName("L1", L1));
  }

  @Test
  void several_candidates_none_matching_the_level_are_ambiguous() {
    givenCandidates(template("Fiche d'engagement L2"), template("Fiche d'engagement M1"));
    assertThrows(ApiException.class, () -> subject.resolveByName("Fiche d'engagement", L1));
  }
}
