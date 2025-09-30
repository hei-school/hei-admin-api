package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Cor;

@Repository
public interface CorRepository extends JpaRepository<Cor, String> {
  List<Cor> findAllByStudentId(String studentId, Pageable pageable);
}
