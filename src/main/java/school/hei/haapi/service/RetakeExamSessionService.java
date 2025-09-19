package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.RetakeExamSessionRepository;

@Component
@AllArgsConstructor
public class RetakeExamSessionService {
  @Autowired RetakeExamSessionRepository retakeExamSessionRepository;

  public RetakeExamSession getById(String id) {
    var session = retakeExamSessionRepository.findById(id);
    if (session.isPresent()) {
      return session.get();
    }
    throw new NotFoundException("Retake exam session not found");
  }
}
