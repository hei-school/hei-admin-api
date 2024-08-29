package school.hei.haapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Letter;

import java.util.List;
import java.util.Optional;

@Repository
public interface LetterRepository extends JpaRepository<Letter, String> {
    List<Letter> findAllByStudentId(String name, Pageable pageable);
}
