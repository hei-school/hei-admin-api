package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.RetakeExamSessionRepository;

@Component
@AllArgsConstructor
public class RetakeExamSessionService {
  private final RetakeExamSessionRepository retakeExamSessionRepository;

  public RetakeExamSession getById(String id) {
    return retakeExamSessionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Retake exam session not found"));
  }
}
