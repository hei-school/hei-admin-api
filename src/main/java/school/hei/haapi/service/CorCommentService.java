package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.CorComment;
import school.hei.haapi.repository.CorCommentRepository;

@Service
@AllArgsConstructor
public class CorCommentService {
  private final CorCommentRepository corCommentRepository;

  public CorComment save(CorComment comment) {
    return corCommentRepository.save(comment);
  }
}
