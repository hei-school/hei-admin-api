package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.DocumensoDocumentGenerationTriggered;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.repository.UserRepository;

@Slf4j
@Service
@AllArgsConstructor
public class DocumensoBulkGenerationService {
  private final UserRepository userRepository;
  private final PromotionRepository promotionRepository;
  private final EventProducer<DocumensoDocumentGenerationTriggered> eventProducer;

  public int generateForPromotion(String promotionId, String templateName, String generatedById) {
    if (!promotionRepository.existsById(promotionId)) {
      throw new NotFoundException("Promotion.id=" + promotionId + " not found");
    }
    var students = userRepository.findAllStudentsByPromotionId(promotionId);

    students.forEach(
        student ->
            eventProducer.accept(
                List.of(
                    DocumensoDocumentGenerationTriggered.builder()
                        .studentId(student.getId())
                        .templateName(templateName)
                        .generatedById(generatedById)
                        .build())));

    log.info(
        "Documenso generation asked for {} students of promotion id={} on template {}",
        students.size(),
        promotionId,
        templateName);
    return students.size();
  }
}
