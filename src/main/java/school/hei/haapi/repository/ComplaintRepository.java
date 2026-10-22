package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Complaint;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, String> {}
