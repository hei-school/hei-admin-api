package school.hei.haapi.service;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.DocumensoDocumentGenerationTriggered;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.repository.UserRepository;

class DocumensoBulkGenerationServiceTest {
  private final UserRepository userRepository = mock(UserRepository.class);
  private final PromotionRepository promotionRepository = mock(PromotionRepository.class);

  @SuppressWarnings("unchecked")
  private final EventProducer<DocumensoDocumentGenerationTriggered> eventProducer =
      mock(EventProducer.class);

  private final DocumensoBulkGenerationService subject =
      new DocumensoBulkGenerationService(userRepository, promotionRepository, eventProducer);

  private static final String ADMIN_ID = randomUUID().toString();

  private static User aStudent() {
    return User.builder().id(randomUUID().toString()).build();
  }

  @Test
  void an_unknown_promotion_is_not_found() {
    when(promotionRepository.existsById("nope")).thenReturn(false);

    var thrown =
        assertThrows(
            NotFoundException.class, () -> subject.generateForPromotion("nope", "Fiche", ADMIN_ID));
    assertEquals("Promotion.id=nope not found", thrown.getMessage());
    verify(eventProducer, never()).accept(any());
  }

  @Test
  void one_event_is_fired_per_student() {
    var students = List.of(aStudent(), aStudent(), aStudent());
    when(promotionRepository.existsById("promo")).thenReturn(true);
    when(userRepository.findAllStudentsByPromotionId("promo")).thenReturn(students);

    assertEquals(3, subject.generateForPromotion("promo", "Fiche d'engagement", ADMIN_ID));

    verify(eventProducer, times(3)).accept(any());
  }

  @Test
  void each_event_carries_its_own_student_and_the_template() {
    var student = aStudent();
    when(promotionRepository.existsById("promo")).thenReturn(true);
    when(userRepository.findAllStudentsByPromotionId("promo")).thenReturn(List.of(student));

    subject.generateForPromotion("promo", "Fiche d'engagement", ADMIN_ID);

    var fired = ArgumentCaptor.forClass(Collection.class);
    verify(eventProducer).accept(fired.capture());
    var event = (DocumensoDocumentGenerationTriggered) List.copyOf(fired.getValue()).getFirst();
    assertEquals(student.getId(), event.getStudentId());
    assertEquals("Fiche d'engagement", event.getTemplateName());
    assertEquals(ADMIN_ID, event.getGeneratedById(), "the asking admin must survive the fan-out");
  }

  @Test
  void an_empty_promotion_fires_nothing() {
    when(promotionRepository.existsById("promo")).thenReturn(true);
    when(userRepository.findAllStudentsByPromotionId("promo")).thenReturn(List.of());

    assertEquals(0, subject.generateForPromotion("promo", "Fiche", ADMIN_ID));
    verify(eventProducer, never()).accept(any());
  }
}
