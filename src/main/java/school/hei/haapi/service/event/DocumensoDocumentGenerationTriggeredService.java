package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.DocumensoDocumentGenerationTriggered;
import school.hei.haapi.service.DocumensoDocumentService;

@Slf4j
@Service
@AllArgsConstructor
public class DocumensoDocumentGenerationTriggeredService
    implements Consumer<DocumensoDocumentGenerationTriggered> {
  private final DocumensoDocumentService documensoDocumentService;

  @Override
  public void accept(DocumensoDocumentGenerationTriggered event) {
    var studentId = event.getStudentId();
    try {
      var document =
          documensoDocumentService.generateDocument(
              studentId, event.getTemplateName(), event.getGeneratedById());
      log.info(
          "Documenso document id={} ready for student id={} from template {}",
          document.getId(),
          studentId,
          event.getTemplateName());
    } catch (Exception e) {
      log.warn(
          "Documenso generation failed for student id={} on template {}: {}",
          studentId,
          event.getTemplateName(),
          e.getMessage());
    }
  }
}
