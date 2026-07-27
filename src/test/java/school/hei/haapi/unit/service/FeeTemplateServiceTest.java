package school.hei.haapi.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.WORK_FEES;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.HARDWARE;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import school.hei.haapi.model.V2FeeTemplate;
import school.hei.haapi.model.V2FeeTemplateContent;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.FeeTemplateRepository;
import school.hei.haapi.repository.V2FeeTemplateRepository;
import school.hei.haapi.repository.dao.FeeTemplateDao;
import school.hei.haapi.service.FeeTemplateService;

class FeeTemplateServiceTest {
  private V2FeeTemplateRepository v2FeeTemplateRepository;
  private FeeTemplateService subject;

  @BeforeEach
  void setUp() {
    v2FeeTemplateRepository = mock();
    FeeTemplateRepository feeTemplateRepository = mock();
    FeeTemplateDao feeTemplateDao = mock();
    subject =
        new FeeTemplateService(v2FeeTemplateRepository, feeTemplateRepository, feeTemplateDao);
  }

  private static V2FeeTemplateContent aContent(String id, String label, long amount) {
    return V2FeeTemplateContent.builder()
        .id(id)
        .label(label)
        .amount(BigInteger.valueOf(amount))
        .dueDate(LocalDate.of(2026, 1, 31))
        .build();
  }

  private static V2FeeTemplate aFeeTemplate(String id) {
    return V2FeeTemplate.builder()
        .id(id)
        .label("Tuition")
        .type(TUITION)
        .category(WORK_FEES)
        .feeTemplateContents(List.of())
        .build();
  }

  @Test
  void getFeeTemplateContentsByTemplateId_returns_contents() {
    var content = aContent("content_id", "January", 5000);
    var feeTemplate =
        aFeeTemplate("template_id").toBuilder().feeTemplateContents(List.of(content)).build();
    when(v2FeeTemplateRepository.findById("template_id")).thenReturn(Optional.of(feeTemplate));

    var actual = subject.getFeeTemplateContentsByTemplateId("template_id");

    assertEquals(List.of(content), actual);
  }

  @Test
  void getFeeTemplateContentsByTemplateId_throws_when_template_not_found() {
    when(v2FeeTemplateRepository.findById("unknown")).thenReturn(Optional.empty());

    var exception =
        assertThrows(
            NotFoundException.class, () -> subject.getFeeTemplateContentsByTemplateId("unknown"));
    assertEquals("FeeTemplate.id=unknown not found", exception.getMessage());
  }

  @Test
  void getV2FeeTemplates_returns_paged_content() {
    var feeTemplate = aFeeTemplate("template_id");
    when(v2FeeTemplateRepository.findAll(PageRequest.of(0, 10)))
        .thenReturn(new PageImpl<>(List.of(feeTemplate)));

    var actual = subject.getV2FeeTemplates(1, 10);

    assertEquals(List.of(feeTemplate), actual);
  }

  @Test
  void crupdateV2FeeTemplates_updates_existing_template() {
    var existing = aFeeTemplate("template_id");
    var provided =
        V2FeeTemplate.builder()
            .id("template_id")
            .label("New label")
            .type(HARDWARE)
            .category(WORK_FEES)
            .build();
    when(v2FeeTemplateRepository.findById("template_id")).thenReturn(Optional.of(existing));
    when(v2FeeTemplateRepository.saveAll(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var actual = subject.crupdateV2FeeTemplates(List.of(provided));

    assertEquals(1, actual.size());
    var saved = actual.getFirst();
    assertEquals("template_id", saved.getId());
    assertEquals("New label", saved.getLabel());
    assertEquals(HARDWARE, saved.getType());
    assertEquals(WORK_FEES, saved.getCategory());
  }

  @Test
  void crupdateV2FeeTemplates_creates_new_template_when_absent() {
    var provided =
        V2FeeTemplate.builder()
            .id("new_id")
            .label("Insurance")
            .type(HARDWARE)
            .category(WORK_FEES)
            .build();
    when(v2FeeTemplateRepository.findById("new_id")).thenReturn(Optional.empty());
    when(v2FeeTemplateRepository.saveAll(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var actual = subject.crupdateV2FeeTemplates(List.of(provided));

    assertEquals(1, actual.size());
    assertEquals("new_id", actual.getFirst().getId());
    assertEquals("Insurance", actual.getFirst().getLabel());
  }

  @Test
  void crupdateV2FeeTemplateContents_saves_and_returns_contents() {
    var feeTemplate = aFeeTemplate("template_id");
    var newContents = List.of(aContent("content_id", "January", 5000));
    when(v2FeeTemplateRepository.findById("template_id")).thenReturn(Optional.of(feeTemplate));
    when(v2FeeTemplateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var actual = subject.crupdateV2FeeTemplateContents("template_id", newContents);

    assertEquals(newContents, actual);
    var captor = ArgumentCaptor.forClass(V2FeeTemplate.class);
    verify(v2FeeTemplateRepository).save(captor.capture());
    assertEquals(newContents, captor.getValue().getFeeTemplateContents());
  }

  @Test
  void crupdateV2FeeTemplateContents_throws_when_template_not_found() {
    when(v2FeeTemplateRepository.findById("unknown")).thenReturn(Optional.empty());

    var exception =
        assertThrows(
            NotFoundException.class,
            () -> subject.crupdateV2FeeTemplateContents("unknown", List.of()));
    assertEquals("Fee template id=unknown not found", exception.getMessage());
  }
}
