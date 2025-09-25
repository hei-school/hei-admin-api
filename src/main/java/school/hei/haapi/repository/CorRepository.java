package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Cor;

@Repository
public interface CorRepository extends JpaRepository<Cor, String> {
  List<Cor> findAllByConcernedStudentId(String concernedStudentId, Pageable pageable);

  Optional<Cor> findByIdAndConcernedStudent_Id(String id, String concernedStudentId);
}
