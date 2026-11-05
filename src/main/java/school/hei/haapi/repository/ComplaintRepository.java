package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Complaint;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, String> {
  List<Complaint> findAllByStudent_IdOrderByCreatedAtDesc(String studentId, Pageable pageable);

  Optional<Complaint> findByIdAndStudent_Id(String id, String studentId);
}
