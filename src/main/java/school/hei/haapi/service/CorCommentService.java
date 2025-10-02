package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.CorComment;
import school.hei.haapi.repository.CorCommentRepository;

@Service
@AllArgsConstructor
public class CorCommentService {
  private final CorCommentRepository corCommentRepository;
  private final CorService corService;

  public CorComment save(CorComment comment) {
    return corCommentRepository.save(comment);
  }

  public CorComment addCommentByCorId(String corId, CorComment comment) {
    return save(comment.toBuilder().cor(corService.getById(corId)).build());
  }
}
