package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Comment;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CommentRepository;

@Service
@AllArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserService userService;

  public List<Comment> getComments(
      String subjectId, String observerId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));

    User subject = userService.findById(subjectId);
    if (Objects.isNull(observerId)) {
      return commentRepository.findAllBySubject(subject, pageable);
    }
    User observer = userService.findById(observerId);
    return commentRepository.findAllBySubjectAndObserver(subject, observer, pageable);
  }

  public Comment postComment(Comment comment) {
    return commentRepository.save(comment);
  }
}
