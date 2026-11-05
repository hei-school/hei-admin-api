package school.hei.haapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.ComplaintAssignee;

@Repository
public interface ComplaintAssigneeRepository extends JpaRepository<ComplaintAssignee, String> {
  boolean existsByComplaint_IdAndUser_Id(String complaintId, String userId);

  Optional<ComplaintAssignee> findByComplaint_IdAndUser_Id(String complaintId, String userId);
}
