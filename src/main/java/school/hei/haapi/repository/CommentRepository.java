package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Comment;
import school.hei.haapi.model.User;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

  List<Comment> findAllBySubject(User subject, Pageable pageable);

  List<Comment> findAllBySubjectAndObserver(User subject, User observer, Pageable pageable);
}
