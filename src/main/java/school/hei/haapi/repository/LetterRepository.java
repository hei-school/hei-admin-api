package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Letter;

@Repository
public interface LetterRepository extends JpaRepository<Letter, String> {
  List<Letter> findAllByStudentId(String name, Pageable pageable);
}
