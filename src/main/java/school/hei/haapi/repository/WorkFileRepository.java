package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.WorkFile;

@Repository
public interface WorkFileRepository extends JpaRepository<WorkFile, String> {
  List<WorkFile> findAllByStudentId(String studentId, Pageable pageable);
}
