package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Transcript;

@Repository
public interface TranscriptRepository extends JpaRepository<Transcript, String> {
  List<Transcript> findAllByStudentId(String studentId, Pageable pageable);

  Optional<Transcript> getByIdAndStudentId(String id, String studentId);
}
