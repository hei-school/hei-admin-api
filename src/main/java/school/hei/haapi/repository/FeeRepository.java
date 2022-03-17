package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Fee;

@Repository
public interface FeeRepository extends JpaRepository<Fee, String> {
  Fee getByStudentIdAndId(String studentId, String feeId);

  //@Query("select f from Fee f where f.student.id = :studentId")
  List<Fee> getByStudentId(String studentId, Pageable pageable);
}
