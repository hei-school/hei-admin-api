package school.hei.haapi.service.event;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.model.DocumensoDocumentGenerationTriggered;
import school.hei.haapi.model.DocumensoDocument;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.DocumensoDocumentService;

class DocumensoDocumentGenerationTriggeredServiceTest {
  private final DocumensoDocumentService documensoDocumentService =
      mock(DocumensoDocumentService.class);
  private final DocumensoDocumentGenerationTriggeredService subject =
      new DocumensoDocumentGenerationTriggeredService(documensoDocumentService);

  private static final String STUDENT_ID = randomUUID().toString();
  private static final String ADMIN_ID = randomUUID().toString();

  private static DocumensoDocumentGenerationTriggered anEvent() {
    return DocumensoDocumentGenerationTriggered.builder()
        .studentId(STUDENT_ID)
        .templateName("Fiche d'engagement")
        .generatedById(ADMIN_ID)
        .build();
  }

  @Test
  void the_event_payload_reaches_the_generation() {
    when(documensoDocumentService.generateDocument(STUDENT_ID, "Fiche d'engagement", ADMIN_ID))
        .thenReturn(DocumensoDocument.builder().id(randomUUID().toString()).build());

    subject.accept(anEvent());

    verify(documensoDocumentService).generateDocument(STUDENT_ID, "Fiche d'engagement", ADMIN_ID);
  }

  @Test
  void a_student_without_monitor_does_not_fail_the_event() {
    when(documensoDocumentService.generateDocument(STUDENT_ID, "Fiche d'engagement", ADMIN_ID))
        .thenThrow(new NotFoundException("No monitor linked to student " + STUDENT_ID));

    assertDoesNotThrow(() -> subject.accept(anEvent()));
  }

  @Test
  void a_documenso_outage_does_not_fail_the_event_either() {
    when(documensoDocumentService.generateDocument(STUDENT_ID, "Fiche d'engagement", ADMIN_ID))
        .thenThrow(
            new ApiException(ApiException.ExceptionType.SERVER_EXCEPTION, "documenso unreachable"));

    assertDoesNotThrow(() -> subject.accept(anEvent()));
  }

  @Test
  void an_unexpected_failure_stays_contained() {
    when(documensoDocumentService.generateDocument(STUDENT_ID, "Fiche d'engagement", ADMIN_ID))
        .thenThrow(new IllegalStateException("boom"));

    assertDoesNotThrow(
        () -> subject.accept(anEvent()),
        "one student's failure must never hold back the rest of the promotion");
  }
}
