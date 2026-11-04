package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.ComplaintAttachment;

@Repository
public interface ComplaintAttachmentRepository extends JpaRepository<ComplaintAttachment, String> {
  List<ComplaintAttachment> findAllByComplaint_IdOrderByCreationDatetimeAsc(String complaintId);
}
